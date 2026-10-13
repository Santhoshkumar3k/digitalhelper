import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { assistantApi } from '../services/api';
import { TranslateResponse } from '../types';
import {
  Languages,
  Mic,
  MicOff,
  Volume2,
  VolumeX,
  Copy,
  Check,
  Sparkles,
  BookOpen,
  Info,
  Loader2,
} from 'lucide-react';

export const TranslatorView: React.FC = () => {
  const { currentLanguage } = useAuth();
  const [inputText, setInputText] = useState<string>('');
  const [targetLang, setTargetLang] = useState<string>(() => (currentLanguage && currentLanguage !== 'en' ? currentLanguage : 'ta'));
  
  useEffect(() => {
    if (currentLanguage && currentLanguage !== 'en') {
      setTargetLang(currentLanguage);
    }
  }, [currentLanguage]);
  const [mode, setMode] = useState<'translate' | 'simplify' | 'explain_terms'>('translate');
  const [depth, setDepth] = useState<'concise' | 'detailed'>('concise');
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [result, setResult] = useState<TranslateResponse | null>(null);
  const [copied, setCopied] = useState<boolean>(false);
  const [isSpeaking, setIsSpeaking] = useState<boolean>(false);
  const [isListening, setIsListening] = useState<boolean>(false);

  const languages = [
    { code: 'ta', name: 'தமிழ் (Tamil)' },
    { code: 'hi', name: 'हिंदी (Hindi)' },
    { code: 'te', name: 'తెలుగు (Telugu)' },
    { code: 'kn', name: 'ಕನ್ನಡ (Kannada)' },
    { code: 'ml', name: 'മലയാളം (Malayalam)' },
    { code: 'bn', name: 'বাংলা (Bengali)' },
    { code: 'mr', name: 'मराठी (Marathi)' },
    { code: 'gu', name: 'ગુજરાતી (Gujarati)' },
    { code: 'en', name: 'Plain English' },
  ];

  const handleTranslate = async () => {
    if (!inputText.trim()) return;
    setIsLoading(true);
    setResult(null);

    try {
      const resp = await assistantApi.translate(inputText.trim(), targetLang, mode, depth);
      setResult(resp);
    } catch (err: any) {
      alert('Translation failed: ' + err.message);
    } finally {
      setIsLoading(false);
    }
  };

  const handleCopy = (text: string) => {
    navigator.clipboard.writeText(text);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  // Text to Speech
  const toggleTTS = (text: string) => {
    if (!('speechSynthesis' in window)) {
      alert('Text to speech is not supported in this browser.');
      return;
    }

    if (isSpeaking) {
      window.speechSynthesis.cancel();
      setIsSpeaking(false);
      return;
    }

    window.speechSynthesis.cancel();
    const utterance = new SpeechSynthesisUtterance(text);
    utterance.rate = 0.9;
    utterance.onend = () => setIsSpeaking(false);
    utterance.onerror = () => setIsSpeaking(false);
    window.speechSynthesis.speak(utterance);
    setIsSpeaking(true);
  };

  // Speech to Text (Microphone)
  const toggleSTT = () => {
    const SpeechRecognition =
      (window as any).SpeechRecognition || (window as any).webkitSpeechRecognition;

    if (!SpeechRecognition) {
      alert('Voice dictation requires Google Chrome or Microsoft Edge.');
      return;
    }

    if (isListening) {
      setIsListening(false);
      return;
    }

    try {
      const recognition = new SpeechRecognition();
      recognition.continuous = false;
      recognition.interimResults = false;
      recognition.lang = targetLang === 'en' ? 'en-IN' : `${targetLang}-IN`;

      recognition.onstart = () => setIsListening(true);
      recognition.onresult = (event: any) => {
        const text = event.results[0][0].transcript;
        setInputText((prev) => (prev ? prev + ' ' + text : text));
        setIsListening(false);
      };
      recognition.onerror = () => setIsListening(false);
      recognition.onend = () => setIsListening(false);

      recognition.start();
    } catch {
      setIsListening(false);
    }
  };

  const loadExample = (example: string, exampleMode: 'translate' | 'simplify' | 'explain_terms') => {
    setInputText(example);
    setMode(exampleMode);
  };

  return (
    <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
      {/* Header */}
      <div className="max-w-3xl space-y-2">
        <div className="inline-flex items-center gap-1.5 bg-emerald-100 dark:bg-emerald-950 text-emerald-700 dark:text-emerald-300 px-3 py-1 rounded-full text-xs font-bold border border-emerald-200 dark:border-emerald-800">
          <Languages className="w-3.5 h-3.5" />
          <span>Indian Languages & Jargon Simplifier</span>
        </div>
        <h1 className="text-3xl font-extrabold text-slate-900 dark:text-white">
          Simple Language & Translation
        </h1>
        <p className="text-sm text-slate-600 dark:text-slate-300">
          Translate official or technical instructions into your native language, rewrite confusing fine-print into 5th-grade words, and get difficult terms explained clearly.
        </p>
      </div>

      {/* Mandatory Legal & Accuracy Disclaimer */}
      <div className="bg-sky-50 dark:bg-sky-950/40 border border-sky-200 dark:border-sky-800 rounded-2xl p-4 flex items-start gap-3">
        <Info className="w-5 h-5 text-sky-600 dark:text-sky-400 shrink-0 mt-0.5" />
        <p className="text-xs text-sky-800 dark:text-sky-300 leading-relaxed">
          <strong>Important Notice:</strong> Translations and simplified rewrites are created for educational, digital-literacy convenience only. They are not certified, legally binding, or official representations of original government documents.
        </p>
      </div>

      {/* Controls Bar: Mode & Language */}
      <div className="flex flex-wrap items-center justify-between gap-4 bg-white dark:bg-slate-900 p-4 rounded-2xl border border-slate-200 dark:border-slate-800 shadow-sm">
        {/* Mode Selector */}
        <div className="flex items-center gap-2">
          <span className="text-xs font-bold text-slate-400 uppercase tracking-wider">Mode:</span>
          <div className="flex bg-slate-100 dark:bg-slate-800 p-1 rounded-xl">
            <button
              onClick={() => setMode('translate')}
              className={`px-3 py-1.5 rounded-lg text-xs font-bold transition ${
                mode === 'translate'
                  ? 'bg-primary-600 text-white shadow-sm'
                  : 'text-slate-600 dark:text-slate-400 hover:text-slate-900 dark:hover:text-white'
              }`}
            >
              Translate Language
            </button>
            <button
              onClick={() => setMode('simplify')}
              className={`px-3 py-1.5 rounded-lg text-xs font-bold transition ${
                mode === 'simplify'
                  ? 'bg-primary-600 text-white shadow-sm'
                  : 'text-slate-600 dark:text-slate-400 hover:text-slate-900 dark:hover:text-white'
              }`}
            >
              Simplify Jargon
            </button>
            <button
              onClick={() => setMode('explain_terms')}
              className={`px-3 py-1.5 rounded-lg text-xs font-bold transition ${
                mode === 'explain_terms'
                  ? 'bg-primary-600 text-white shadow-sm'
                  : 'text-slate-600 dark:text-slate-400 hover:text-slate-900 dark:hover:text-white'
              }`}
            >
              Explain Technical Terms
            </button>
          </div>
        </div>

        {/* Target Language & Depth */}
        <div className="flex items-center gap-3">
          <div className="flex items-center gap-2">
            <span className="text-xs font-bold text-slate-400 uppercase tracking-wider">Language:</span>
            <select
              value={targetLang}
              onChange={(e) => setTargetLang(e.target.value)}
              className="bg-slate-100 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 text-slate-900 dark:text-white text-xs font-bold py-1.5 px-3 rounded-xl focus:outline-none focus:ring-2 focus:ring-primary-500"
            >
              {languages.map((l) => (
                <option key={l.code} value={l.code} className="bg-slate-900 text-white">
                  {l.name}
                </option>
              ))}
            </select>
          </div>

          <div className="flex bg-slate-100 dark:bg-slate-800 p-1 rounded-xl">
            <button
              onClick={() => setDepth('concise')}
              className={`px-2.5 py-1 rounded-lg text-xs font-semibold ${
                depth === 'concise' ? 'bg-white dark:bg-slate-700 text-slate-900 dark:text-white shadow-xs' : 'text-slate-500'
              }`}
            >
              Concise
            </button>
            <button
              onClick={() => setDepth('detailed')}
              className={`px-2.5 py-1 rounded-lg text-xs font-semibold ${
                depth === 'detailed' ? 'bg-white dark:bg-slate-700 text-slate-900 dark:text-white shadow-xs' : 'text-slate-500'
              }`}
            >
              Detailed
            </button>
          </div>
        </div>
      </div>

      {/* Main Translation Two-Column Box */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Input Column */}
        <div className="bg-white dark:bg-slate-900 rounded-2xl border border-slate-200 dark:border-slate-800 p-5 shadow-sm flex flex-col justify-between space-y-4">
          <div>
            <div className="flex items-center justify-between mb-2">
              <span className="text-xs font-bold text-slate-500 uppercase tracking-wider">
                Original Text / Complex Instructions
              </span>
              <button
                onClick={toggleSTT}
                className={`p-1.5 rounded-lg text-xs flex items-center gap-1 font-semibold transition ${
                  isListening
                    ? 'bg-rose-500 text-white animate-pulse'
                    : 'text-slate-500 hover:bg-slate-100 dark:hover:bg-slate-800'
                }`}
                title="Dictate with voice"
              >
                {isListening ? <MicOff className="w-3.5 h-3.5" /> : <Mic className="w-3.5 h-3.5" />}
                <span>{isListening ? 'Listening...' : 'Dictate'}</span>
              </button>
            </div>

            <textarea
              rows={8}
              value={inputText}
              onChange={(e) => setInputText(e.target.value)}
              placeholder="Paste confusing SMS, bank notice, legal terms, or app error here..."
              className="w-full text-xs sm:text-sm p-3.5 rounded-xl border border-slate-200 dark:border-slate-700 bg-slate-50 dark:bg-slate-800/50 text-slate-900 dark:text-white placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-primary-500 resize-none leading-relaxed"
            />
          </div>

          <div className="space-y-3">
            {/* Quick Example Chips */}
            <div className="space-y-1">
              <span className="text-[10px] font-bold text-slate-400 uppercase tracking-wider">
                Try quick examples:
              </span>
              <div className="flex flex-wrap gap-1.5">
                <button
                  onClick={() =>
                    loadExample(
                      'Your KYC mandate has lapsed. Please furnish biometric authentication credentials to avert service suspension.',
                      'simplify'
                    )
                  }
                  className="text-[11px] bg-slate-100 dark:bg-slate-800 hover:bg-slate-200 px-2.5 py-1 rounded-lg text-slate-700 dark:text-slate-300"
                >
                  Bank KYC Notice
                </button>
                <button
                  onClick={() =>
                    loadExample(
                      'Clear browser application cache and DNS resolver cookies to mitigate gateway timeout 504.',
                      'translate'
                    )
                  }
                  className="text-[11px] bg-slate-100 dark:bg-slate-800 hover:bg-slate-200 px-2.5 py-1 rounded-lg text-slate-700 dark:text-slate-300"
                >
                  Technical Error
                </button>
                <button
                  onClick={() => loadExample('What is Two-Factor Authentication and Phishing?', 'explain_terms')}
                  className="text-[11px] bg-slate-100 dark:bg-slate-800 hover:bg-slate-200 px-2.5 py-1 rounded-lg text-slate-700 dark:text-slate-300"
                >
                  Explain 2FA & Phishing
                </button>
              </div>
            </div>

            <button
              onClick={handleTranslate}
              disabled={!inputText.trim() || isLoading}
              className="w-full bg-primary-600 hover:bg-primary-700 disabled:opacity-40 disabled:cursor-not-allowed text-white font-bold py-3 rounded-xl text-xs sm:text-sm flex items-center justify-center gap-2 shadow-md shadow-primary-500/20 transition cursor-pointer"
            >
              {isLoading ? (
                <>
                  <Loader2 className="w-4 h-4 animate-spin" />
                  <span>Translating & simplifying...</span>
                </>
              ) : (
                <>
                  <Sparkles className="w-4 h-4" />
                  <span>
                    {mode === 'translate'
                      ? 'Translate to ' + languages.find((l) => l.code === targetLang)?.name
                      : mode === 'simplify'
                      ? 'Rewrite into Simple Words'
                      : 'Explain Difficult Terms'}
                  </span>
                </>
              )}
            </button>
          </div>
        </div>

        {/* Output Column */}
        <div className="bg-white dark:bg-slate-900 rounded-2xl border border-slate-200 dark:border-slate-800 p-5 shadow-sm flex flex-col justify-between space-y-4">
          <div>
            <div className="flex items-center justify-between mb-2">
              <span className="text-xs font-bold text-slate-500 uppercase tracking-wider">
                Simplified Result ({languages.find((l) => l.code === targetLang)?.name})
              </span>

              {result && (
                <div className="flex items-center gap-1.5">
                  <button
                    onClick={() => toggleTTS(result.translatedText || result.simplifiedText)}
                    className="p-1.5 rounded-lg text-slate-500 hover:text-primary-600 hover:bg-slate-100 dark:hover:bg-slate-800 transition"
                    title="Listen aloud (Text-to-Speech)"
                  >
                    {isSpeaking ? <VolumeX className="w-4 h-4 text-primary-500" /> : <Volume2 className="w-4 h-4" />}
                  </button>
                  <button
                    onClick={() => handleCopy(result.translatedText || result.simplifiedText)}
                    className="p-1.5 rounded-lg text-slate-500 hover:text-primary-600 hover:bg-slate-100 dark:hover:bg-slate-800 transition"
                    title="Copy translation"
                  >
                    {copied ? <Check className="w-4 h-4 text-emerald-500" /> : <Copy className="w-4 h-4" />}
                  </button>
                </div>
              )}
            </div>

            {result ? (
              <div className="space-y-4">
                {/* Translated text */}
                <div className="p-4 rounded-xl bg-slate-50 dark:bg-slate-800/60 border border-slate-200 dark:border-slate-700 text-xs sm:text-sm text-slate-900 dark:text-slate-100 leading-relaxed font-medium">
                  {result.translatedText}
                </div>

                {/* Plain words version */}
                {result.simplifiedText && result.simplifiedText !== result.translatedText && (
                  <div className="space-y-1">
                    <span className="text-[11px] font-bold text-slate-400 uppercase tracking-wider">
                      Everyday Explanation:
                    </span>
                    <div className="p-3.5 rounded-xl bg-emerald-50/70 dark:bg-emerald-950/30 border border-emerald-200 dark:border-emerald-900/60 text-xs text-slate-800 dark:text-slate-200 leading-relaxed">
                      {result.simplifiedText}
                    </div>
                  </div>
                )}

                {/* Difficult terms dictionary breakdown */}
                {result.difficultTermsExplained && Object.keys(result.difficultTermsExplained).length > 0 && (
                  <div className="space-y-2 pt-2 border-t border-slate-100 dark:border-slate-800">
                    <span className="text-xs font-bold text-slate-700 dark:text-slate-300 flex items-center gap-1.5">
                      <BookOpen className="w-3.5 h-3.5 text-primary-500" />
                      <span>Key Technical Terms Explained:</span>
                    </span>
                    <div className="space-y-2">
                      {Object.entries(result.difficultTermsExplained).map(([term, desc], idx) => (
                        <div key={idx} className="p-2.5 rounded-lg bg-slate-100 dark:bg-slate-800 text-xs">
                          <strong className="text-primary-600 dark:text-primary-400 block mb-0.5">{term}</strong>
                          <span className="text-slate-600 dark:text-slate-300">{desc}</span>
                        </div>
                      ))}
                    </div>
                  </div>
                )}
              </div>
            ) : (
              <div className="h-64 flex flex-col items-center justify-center text-center text-slate-400 space-y-2">
                <Languages className="w-10 h-10 text-slate-300 dark:text-slate-700" />
                <p className="text-xs">
                  Enter or paste text on the left and tap translate.
                </p>
              </div>
            )}
          </div>

          <div className="text-[10px] text-slate-400 border-t border-slate-100 dark:border-slate-800 pt-3">
            Supports Tamil, Kannada, Hindi, Telugu, Malayalam, Bengali, Marathi, Gujarati, and English with built-in voice playback.
          </div>
        </div>
      </div>
    </div>
  );
};
