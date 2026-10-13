import React, { useState, useEffect, useRef } from 'react';
import { useAuth } from '../context/AuthContext';
import { assistantApi, savedAnswerApi, feedbackApi } from '../services/api';
import { ConversationSummary, ConversationDetail, Message } from '../types';
import {
  Send,
  Mic,
  MicOff,
  Volume2,
  VolumeX,
  Copy,
  Check,
  Bookmark,
  ThumbsUp,
  ThumbsDown,
  RotateCcw,
  PlusCircle,
  Trash2,
  Sparkles,
  Bot,
  User as UserIcon,
  HelpCircle,
  Loader2,
  AlertCircle,
  MessageSquare,
} from 'lucide-react';

interface ChatAssistantViewProps {
  initialPrompt?: string;
  onClearInitialPrompt?: () => void;
  onOpenSettings?: () => void;
}

export const ChatAssistantView: React.FC<ChatAssistantViewProps> = ({
  initialPrompt,
  onClearInitialPrompt,
  onOpenSettings,
}) => {
  const { user, currentLanguage } = useAuth();
  const [conversations, setConversations] = useState<ConversationSummary[]>([]);
  const [currentConversationId, setCurrentConversationId] = useState<number | null>(null);
  const [messages, setMessages] = useState<Message[]>([]);
  const [inputPrompt, setInputPrompt] = useState<string>('');
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [followUps, setFollowUps] = useState<string[]>([]);
  const [copiedId, setCopiedId] = useState<number | null>(null);
  const [savedId, setSavedId] = useState<number | null>(null);
  const [speakingId, setSpeakingId] = useState<number | null>(null);
  const [isRecording, setIsRecording] = useState<boolean>(false);
  const [feedbackGiven, setFeedbackGiven] = useState<Record<number, boolean>>({});
  const [aiStatus, setAiStatus] = useState<{ configured: boolean; providerName: string } | null>(null);

  const messagesEndRef = useRef<HTMLDivElement>(null);
  const chatStreamRef = useRef<HTMLDivElement>(null);
  const speechRef = useRef<SpeechSynthesisUtterance | null>(null);

  // Auto-scroll to bottom of chat only when active messages exist
  useEffect(() => {
    if (messages.length > 0) {
      messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
    } else if (chatStreamRef.current) {
      chatStreamRef.current.scrollTop = 0;
    }
  }, [messages, isLoading]);

  // Load user conversations & AI status on mount
  useEffect(() => {
    loadConversations();
    assistantApi.getAiStatus().then(setAiStatus).catch(() => {});
  }, [user]);

  // Handle initial prompt from search if passed
  useEffect(() => {
    if (initialPrompt && initialPrompt.trim()) {
      const promptToSend = initialPrompt.trim();
      if (onClearInitialPrompt) onClearInitialPrompt();
      handleSendMessage(promptToSend);
    }
  }, [initialPrompt]);

  const loadConversations = async () => {
    if (!user) return;
    try {
      const convList = await assistantApi.getConversations();
      setConversations(convList);
    } catch {
      // ignore
    }
  };

  const handleSelectConversation = async (id: number) => {
    try {
      setIsLoading(true);
      const detail: ConversationDetail = await assistantApi.getConversation(id);
      setCurrentConversationId(detail.id);
      setMessages(detail.messages);
      setFollowUps([]);
    } catch (err: any) {
      alert('Failed to load conversation: ' + err.message);
    } finally {
      setIsLoading(false);
    }
  };

  const handleNewConversation = () => {
    setCurrentConversationId(null);
    setMessages([]);
    setFollowUps([]);
    setInputPrompt('');
  };

  const handleDeleteConversation = async (e: React.MouseEvent, id: number) => {
    e.stopPropagation();
    if (!confirm('Are you sure you want to delete this conversation?')) return;
    try {
      await assistantApi.deleteConversation(id);
      setConversations((prev) => prev.filter((c) => c.id !== id));
      if (currentConversationId === id) {
        handleNewConversation();
      }
    } catch (err: any) {
      alert('Could not delete: ' + err.message);
    }
  };

  const handleSendMessage = async (textToSend?: string) => {
    const text = (textToSend || inputPrompt).trim();
    if (!text || isLoading) return;

    const tempUserMsg: Message = {
      id: Date.now(),
      role: 'user',
      content: text,
      createdAt: new Date().toISOString(),
    };

    setMessages((prev) => [...prev, tempUserMsg]);
    setInputPrompt('');
    setIsLoading(true);

    try {
      const previousMsg = messages.length > 0 ? messages[messages.length - 1].content : undefined;
      const resp = await assistantApi.ask(text, currentConversationId || undefined, currentLanguage, undefined, previousMsg);
      setCurrentConversationId(resp.conversationId);

      const assistantMsg: Message = {
        id: resp.messageId || Date.now() + 1,
        role: 'assistant',
        content: resp.answer,
        createdAt: new Date().toISOString(),
      };

      setMessages((prev) => [...prev, assistantMsg]);
      setFollowUps(resp.followUpSuggestions || []);
      loadConversations();
    } catch (err: any) {
      const errorMsg: Message = {
        id: Date.now() + 2,
        role: 'assistant',
        content: `⚠️ Sorry, I could not complete that request: ${err.message}. Please check if the backend server is running and try again.`,
        createdAt: new Date().toISOString(),
      };
      setMessages((prev) => [...prev, errorMsg]);
    } finally {
      setIsLoading(false);
    }
  };

  const handleKeyDown = (e: React.KeyboardEvent<HTMLTextAreaElement>) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSendMessage();
    }
  };

  // Text to Speech
  const toggleTextToSpeech = (msgId: number, text: string) => {
    if (!('speechSynthesis' in window)) {
      alert('Speech synthesis is not supported in this browser.');
      return;
    }

    if (speakingId === msgId) {
      window.speechSynthesis.cancel();
      setSpeakingId(null);
      return;
    }

    window.speechSynthesis.cancel();
    const cleanText = text.replace(/[*#_`]/g, '');
    const utterance = new SpeechSynthesisUtterance(cleanText);
    utterance.rate = 0.95; // Friendly, clear pacing
    utterance.lang = currentLanguage === 'en' ? 'en-IN' : (currentLanguage + '-IN');
    utterance.onend = () => setSpeakingId(null);
    utterance.onerror = () => setSpeakingId(null);
    speechRef.current = utterance;
    window.speechSynthesis.speak(utterance);
    setSpeakingId(msgId);
  };

  // Speech to Text (Microphone)
  const toggleSpeechRecognition = () => {
    const SpeechRecognition =
      (window as any).SpeechRecognition || (window as any).webkitSpeechRecognition;

    if (!SpeechRecognition) {
      alert('Voice dictation is not supported in this browser. Please use Chrome or Edge.');
      return;
    }

    if (isRecording) {
      setIsRecording(false);
      return;
    }

    try {
      const recognition = new SpeechRecognition();
      recognition.continuous = false;
      recognition.interimResults = false;
      recognition.lang = currentLanguage === 'en' ? 'en-IN' : (currentLanguage + '-IN');

      recognition.onstart = () => setIsRecording(true);
      recognition.onresult = (event: any) => {
        const transcript = event.results[0][0].transcript;
        setInputPrompt((prev) => (prev ? prev + ' ' + transcript : transcript));
        setIsRecording(false);
      };
      recognition.onerror = () => setIsRecording(false);
      recognition.onend = () => setIsRecording(false);

      recognition.start();
    } catch {
      setIsRecording(false);
    }
  };

  // Copy answer to clipboard
  const handleCopyAnswer = (id: number, text: string) => {
    navigator.clipboard.writeText(text);
    setCopiedId(id);
    setTimeout(() => setCopiedId(null), 2000);
  };

  // Bookmark / Save answer
  const handleSaveAnswer = async (id: number, content: string) => {
    try {
      await savedAnswerApi.save({
        title: content.substring(0, 50).replace(/[#*]/g, '').trim() + '...',
        content: content,
        category: 'Everyday Tech',
      });
      setSavedId(id);
      setTimeout(() => setSavedId(null), 2500);
    } catch (err: any) {
      alert('Please sign in to save answers to your bookmarks!');
    }
  };

  // Feedback on answer
  const handleFeedback = async (messageId: number, isHelpful: boolean) => {
    try {
      await feedbackApi.submit({
        conversationId: currentConversationId || undefined,
        messageId,
        rating: isHelpful ? 5 : 2,
        helpful: isHelpful,
        comments: isHelpful ? 'Helpful answer' : 'Needs more clarity',
        feature: 'chat',
      });
      setFeedbackGiven((prev) => ({ ...prev, [messageId]: true }));
    } catch {
      // ignore
    }
  };

  return (
    <div className="w-full max-w-7xl mx-auto px-2 sm:px-6 py-2 sm:py-3 h-[calc(100vh-6.5rem)] max-h-[calc(100dvh-6.5rem)] flex flex-col md:flex-row gap-3 sm:gap-4 overflow-hidden">
      {/* Sidebar: Conversation History (Desktop & Tablet) */}
      <aside aria-label="Conversation history" className="hidden md:flex w-72 flex-col h-full bg-slate-100 dark:bg-slate-900 rounded-2xl border-2 border-slate-300 dark:border-slate-700 shadow-sm overflow-hidden shrink-0">
        <div className="shrink-0 p-3 border-b-2 border-slate-300 dark:border-slate-700 flex items-center justify-between bg-slate-200/60 dark:bg-slate-800/80">
          <h3 className="font-bold text-xs uppercase tracking-wider text-slate-700 dark:text-slate-300 flex items-center gap-1.5">
            <MessageSquare className="w-3.5 h-3.5 text-orange-500" />
            <span>Chat History</span>
          </h3>
          <button
            onClick={handleNewConversation}
            className="p-1.5 rounded-lg bg-orange-100 dark:bg-slate-800 text-orange-600 dark:text-orange-400 hover:bg-orange-200 text-xs font-semibold flex items-center gap-1 cursor-pointer border border-orange-200 dark:border-slate-700"
            title="Start new conversation"
          >
            <PlusCircle className="w-4 h-4" />
            <span>New</span>
          </button>
        </div>

        <div className="flex-1 min-h-0 overflow-y-auto p-2 space-y-1">
          {conversations.length === 0 ? (
            <div className="p-6 text-center text-slate-500 dark:text-slate-400 text-xs font-medium">
              No conversations yet. Ask your first question!
            </div>
          ) : (
            conversations.map((c) => {
              const isSelected = currentConversationId === c.id;
              return (
                <div
                  key={c.id}
                  onClick={() => handleSelectConversation(c.id)}
                  className={`p-2.5 rounded-xl text-xs cursor-pointer flex items-center justify-between group transition ${
                    isSelected
                      ? 'bg-orange-100 dark:bg-slate-800 border-2 border-orange-400 dark:border-orange-500 text-orange-950 dark:text-orange-200 font-bold'
                      : 'text-slate-800 dark:text-slate-200 hover:bg-slate-200/70 dark:hover:bg-slate-800 border border-transparent'
                  }`}
                >
                  <div className="truncate pr-2">
                    <p className="truncate font-bold">{c.title}</p>
                    <span className="text-[10px] text-slate-500 dark:text-slate-400 block font-normal">
                      {c.category}
                    </span>
                  </div>
                  <button
                    onClick={(e) => handleDeleteConversation(e, c.id)}
                    className="opacity-0 group-hover:opacity-100 p-1 text-slate-400 hover:text-rose-500 transition cursor-pointer"
                    title="Delete conversation"
                  >
                    <Trash2 className="w-3.5 h-3.5" />
                  </button>
                </div>
              );
            })
          )}
        </div>
      </aside>

      {/* Main Chat Area */}
      <main aria-label="AI Chat Assistant" className="flex-1 flex flex-col h-full min-h-0 bg-slate-100 dark:bg-slate-900 rounded-2xl border-2 border-slate-300 dark:border-slate-700 shadow-sm overflow-hidden">
        {/* Chat Header */}
        <div className="shrink-0 px-4 py-2.5 sm:px-5 sm:py-3 border-b-2 border-slate-300 dark:border-slate-700 flex items-center justify-between bg-slate-200/60 dark:bg-slate-800/80">
          <div className="flex items-center gap-3">
            <div className="w-9 h-9 rounded-xl bg-gradient-to-tr from-orange-500 via-rose-500 to-amber-500 flex items-center justify-center text-white shadow-sm">
              <Bot className="w-5 h-5" />
            </div>
            <div>
              <h2 className="text-sm font-bold text-slate-950 dark:text-white flex flex-wrap items-center gap-1.5 sm:gap-2">
                <span>DigitalHelper Assistant</span>
                <span className="text-[10px] bg-emerald-100 dark:bg-emerald-950 text-emerald-800 dark:text-emerald-300 px-2 py-0.5 rounded-full font-bold border border-emerald-300 dark:border-emerald-800">
                  Online & Ready
                </span>
                {onOpenSettings && (
                  <button
                    type="button"
                    onClick={onOpenSettings}
                    className="text-[10px] bg-orange-100 dark:bg-slate-800 hover:bg-orange-200 text-orange-950 dark:text-orange-300 px-2 py-0.5 rounded-full font-black border border-orange-300 dark:border-slate-700 cursor-pointer flex items-center gap-1"
                    title="Configure AI Model (Gemini / ChatGPT / Local)"
                  >
                    <Sparkles className="w-3 h-3 text-orange-500" />
                    <span>{aiStatus?.configured ? aiStatus.providerName : 'Engine: Gemini / Smart AI'}</span>
                  </button>
                )}
              </h2>
              <p className="text-[11px] text-slate-600 dark:text-slate-400 font-medium">
                Ask in plain everyday words • Never share bank passwords or OTPs
              </p>
            </div>
          </div>

          <button
            onClick={handleNewConversation}
            className="md:hidden text-xs bg-orange-100 dark:bg-slate-800 text-orange-600 dark:text-orange-400 px-3 py-1.5 rounded-lg font-bold flex items-center gap-1 cursor-pointer border border-orange-300 dark:border-slate-700"
          >
            <PlusCircle className="w-4 h-4" /> New Chat
          </button>
        </div>

        {/* Message Stream */}
        <div ref={chatStreamRef} className="flex-1 min-h-0 overflow-y-auto p-3 sm:p-5 space-y-4">
          {messages.length === 0 ? (
            <div className="h-full flex flex-col items-center justify-center text-center max-w-xl mx-auto py-2 space-y-2.5 overflow-hidden">
              <div className="w-11 h-11 rounded-2xl bg-orange-100 dark:bg-slate-800 text-orange-600 dark:text-orange-400 flex items-center justify-center shadow-xs border border-orange-200 dark:border-slate-700 shrink-0">
                <Sparkles className="w-5 h-5" />
              </div>
              <div>
                <h3 className="text-base sm:text-lg font-black text-slate-950 dark:text-white">
                  How can I guide you today?
                </h3>
                <p className="text-xs text-slate-600 dark:text-slate-300 leading-relaxed max-w-md mx-auto mt-0.5 font-medium">
                  Ask about your phone, WhatsApp, computer, printer, payments, or confusing error messages in plain words.
                </p>
              </div>

              {/* 2x2 Compact non-scrollable preset starter questions */}
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-2 w-full pt-1">
                {[
                  'How do I clear my WhatsApp storage?',
                  'Why does my phone get hot while charging?',
                  'How do I take a screenshot on my computer?',
                  'What is an OTP and why should I never share it?',
                ].map((preset, idx) => (
                  <button
                    key={idx}
                    onClick={() => handleSendMessage(preset)}
                    className="text-xs p-2.5 rounded-xl border-2 border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 hover:bg-orange-50 dark:hover:bg-slate-700/80 hover:border-orange-400 text-slate-900 dark:text-slate-100 flex items-center justify-between transition cursor-pointer font-bold shadow-2xs text-left"
                  >
                    <span className="line-clamp-2 pr-1">{preset}</span>
                    <Send className="w-3.5 h-3.5 text-orange-500 shrink-0" />
                  </button>
                ))}
              </div>
            </div>
          ) : (
            messages.map((m) => {
              const isUser = m.role === 'user';
              return (
                <div
                  key={m.id}
                  className={`flex gap-3 sm:gap-4 ${isUser ? 'justify-end' : 'justify-start'}`}
                >
                  {!isUser && (
                    <div className="w-8 h-8 rounded-xl bg-gradient-to-tr from-orange-500 via-rose-500 to-amber-500 flex items-center justify-center text-white shrink-0 shadow-sm mt-1">
                      <Bot className="w-4 h-4" />
                    </div>
                  )}

                  <div className="max-w-[85%] sm:max-w-[78%] space-y-2">
                    <div
                      className={`p-3.5 sm:p-4 rounded-2xl text-xs sm:text-sm leading-relaxed ${
                        isUser
                          ? 'bg-orange-500 text-white rounded-tr-none shadow-md font-medium'
                          : 'bg-white dark:bg-slate-800 text-slate-950 dark:text-slate-100 rounded-tl-none border-2 border-slate-300 dark:border-slate-700 shadow-sm'
                      }`}
                    >
                      <div className="whitespace-pre-wrap font-sans space-y-2">
                        {m.content}
                      </div>
                    </div>

                    {/* Actions bar for Assistant response */}
                    {!isUser && (
                      <div className="flex items-center gap-1 text-slate-500 dark:text-slate-400 text-xs px-1">
                        <button
                          onClick={() => toggleTextToSpeech(m.id, m.content)}
                          className={`p-1.5 rounded-lg hover:bg-slate-200 dark:hover:bg-slate-700 transition cursor-pointer ${
                            speakingId === m.id ? 'text-orange-600 font-bold bg-orange-100 dark:bg-slate-700' : ''
                          }`}
                          title="Read answer aloud"
                        >
                          {speakingId === m.id ? <VolumeX className="w-4 h-4" /> : <Volume2 className="w-4 h-4" />}
                        </button>

                        <button
                          onClick={() => handleCopyAnswer(m.id, m.content)}
                          className="p-1.5 rounded-lg hover:bg-slate-200 dark:hover:bg-slate-700 transition text-slate-600 hover:text-slate-950 dark:text-slate-300 dark:hover:text-white cursor-pointer"
                          title="Copy answer"
                        >
                          {copiedId === m.id ? <Check className="w-4 h-4 text-emerald-500" /> : <Copy className="w-4 h-4" />}
                        </button>

                        <button
                          onClick={() => handleSaveAnswer(m.id, m.content)}
                          className="p-1.5 rounded-lg hover:bg-slate-200 dark:hover:bg-slate-700 transition text-slate-600 hover:text-amber-500 dark:text-slate-300 cursor-pointer"
                          title="Save to bookmarks"
                        >
                          {savedId === m.id ? <Check className="w-4 h-4 text-amber-500" /> : <Bookmark className="w-4 h-4" />}
                        </button>

                        <span className="text-slate-300 dark:text-slate-700">|</span>

                        {feedbackGiven[m.id] ? (
                          <span className="text-[11px] text-emerald-600 dark:text-emerald-400 font-bold px-2">
                            Thanks for rating!
                          </span>
                        ) : (
                          <>
                            <button
                              onClick={() => handleFeedback(m.id, true)}
                              className="p-1.5 rounded-lg hover:bg-slate-200 dark:hover:bg-slate-800 text-slate-500 hover:text-emerald-500 transition"
                              title="Helpful answer"
                            >
                              <ThumbsUp className="w-3.5 h-3.5" />
                            </button>
                            <button
                              onClick={() => handleFeedback(m.id, false)}
                              className="p-1.5 rounded-lg hover:bg-slate-200 dark:hover:bg-slate-800 text-slate-500 hover:text-rose-500 transition"
                              title="Needs improvement"
                            >
                              <ThumbsDown className="w-3.5 h-3.5" />
                            </button>
                          </>
                        )}
                      </div>
                    )}
                  </div>

                  {isUser && (
                    <div className="w-8 h-8 rounded-xl bg-slate-200 dark:bg-slate-700 flex items-center justify-center text-slate-700 dark:text-slate-200 shrink-0 mt-1">
                      <UserIcon className="w-4 h-4" />
                    </div>
                  )}
                </div>
              );
            })
          )}

          {isLoading && (
            <div className="flex gap-3 items-center text-slate-500 text-xs py-2">
              <div className="w-8 h-8 rounded-xl bg-primary-600 flex items-center justify-center text-white shrink-0 animate-pulse">
                <Bot className="w-4 h-4" />
              </div>
              <div className="bg-slate-100 dark:bg-slate-800 px-4 py-3 rounded-2xl flex items-center gap-2 border border-slate-200 dark:border-slate-700">
                <Loader2 className="w-4 h-4 animate-spin text-primary-500" />
                <span>DigitalHelper is preparing clear, step-by-step guidance...</span>
              </div>
            </div>
          )}

          {/* Follow-up Suggestion Chips */}
          {followUps.length > 0 && !isLoading && (
            <div className="pt-2 space-y-1.5">
              <span className="text-[11px] font-bold text-slate-500 dark:text-slate-400 uppercase tracking-wider block">
                Suggested follow-up questions:
              </span>
              <div className="flex flex-wrap gap-2">
                {followUps.map((f, i) => (
                  <button
                    key={i}
                    onClick={() => handleSendMessage(f)}
                    className="text-xs bg-orange-50 dark:bg-slate-800 hover:bg-orange-100 dark:hover:bg-slate-700 text-orange-900 dark:text-orange-300 px-3 py-1.5 rounded-full border-2 border-orange-200 dark:border-slate-700 transition cursor-pointer font-bold"
                  >
                    + {f}
                  </button>
                ))}
              </div>
            </div>
          )}

          <div ref={messagesEndRef} />
        </div>

        {/* Small & Compact Input Bar */}
        <div className="shrink-0 p-2 sm:p-2.5 border-t-2 border-slate-300 dark:border-slate-700 bg-slate-200/60 dark:bg-slate-800/80">
          <form
            onSubmit={(e) => {
              e.preventDefault();
              handleSendMessage();
            }}
            className="flex items-center gap-2 rounded-xl bg-white dark:bg-slate-900 border-2 border-slate-300 dark:border-slate-700 px-3 py-1.5 focus-within:border-orange-500 shadow-2xs"
          >
            <input
              type="text"
              value={inputPrompt}
              onChange={(e) => setInputPrompt(e.target.value)}
              placeholder="Ask anything (e.g., 'How do I stop WhatsApp from saving photos?')..."
              className="flex-1 py-1 text-xs sm:text-sm bg-transparent text-slate-950 dark:text-white placeholder-slate-400 dark:placeholder-slate-500 focus:outline-none font-medium"
            />

            {/* Mic Speech-to-Text Button */}
            <button
              type="button"
              onClick={toggleSpeechRecognition}
              className={`p-1.5 rounded-lg transition cursor-pointer shrink-0 ${
                isRecording
                  ? 'bg-rose-500 text-white animate-pulse'
                  : 'text-slate-400 hover:text-slate-600 dark:hover:text-slate-200 hover:bg-slate-100 dark:hover:bg-slate-800'
              }`}
              title={isRecording ? 'Listening... click to stop' : 'Click to speak your question'}
            >
              {isRecording ? <MicOff className="w-4 h-4" /> : <Mic className="w-4 h-4" />}
            </button>

            {/* Send Button */}
            <button
              type="submit"
              disabled={!inputPrompt.trim() || isLoading}
              className="bg-orange-500 hover:bg-orange-600 disabled:opacity-40 disabled:cursor-not-allowed text-white px-3.5 py-1.5 rounded-lg font-black text-xs flex items-center gap-1.5 shadow-xs transition cursor-pointer shrink-0"
            >
              <span>Send</span>
              <Send className="w-3.5 h-3.5" />
            </button>
          </form>
          <div className="mt-1 flex justify-between text-[10px] text-slate-500 dark:text-slate-400 px-1 font-medium">
            <span>Press Enter to send</span>
            <span>Voice dictation & Audio playback supported</span>
          </div>
        </div>
      </main>
    </div>
  );
};
