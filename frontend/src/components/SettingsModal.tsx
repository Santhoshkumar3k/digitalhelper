import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { assistantApi } from '../services/api';
import {
  X,
  Settings,
  Globe,
  Eye,
  Type,
  Sun,
  Moon,
  Trash2,
  Sparkles,
  Key,
  CheckCircle2,
  ExternalLink,
  Cpu,
  Loader2,
} from 'lucide-react';

interface SettingsModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export const SettingsModal: React.FC<SettingsModalProps> = ({ isOpen, onClose }) => {
  const {
    user,
    preferences,
    updatePreferences,
    deleteAccount,
    fontSize,
    setFontSize,
    highContrast,
    setHighContrast,
    darkMode,
    setDarkMode,
  } = useAuth();

  const [isDeleting, setIsDeleting] = useState(false);
  const [deleteConfirm, setDeleteConfirm] = useState(false);

  // AI Configuration state
  const [aiStatus, setAiStatus] = useState<{
    configured: boolean;
    provider: string;
    providerName: string;
    model: string;
    maskedApiKey: string;
  } | null>(null);
  const [selectedProvider, setSelectedProvider] = useState<'gemini' | 'openai' | 'groq'>('gemini');
  const [customApiKey, setCustomApiKey] = useState('');
  const [isSavingAi, setIsSavingAi] = useState(false);
  const [aiSaveMsg, setAiSaveMsg] = useState<{ type: 'success' | 'error'; text: string } | null>(null);

  useEffect(() => {
    if (isOpen) {
      loadAiStatus();
    }
  }, [isOpen]);

  const loadAiStatus = async () => {
    try {
      const status = await assistantApi.getAiStatus();
      setAiStatus(status);
      if (status.provider && (status.provider === 'gemini' || status.provider === 'openai' || status.provider === 'groq')) {
        setSelectedProvider(status.provider as any);
      }
    } catch {
      // ignore
    }
  };

  const handleSaveAiConfig = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!customApiKey.trim()) {
      setAiSaveMsg({ type: 'error', text: 'Please enter a valid API key.' });
      return;
    }
    setIsSavingAi(true);
    setAiSaveMsg(null);
    try {
      const res = await assistantApi.configureAi({
        provider: selectedProvider,
        apiKey: customApiKey.trim(),
      });
      setAiStatus(res);
      setCustomApiKey('');
      setAiSaveMsg({ type: 'success', text: `Activated ${res.providerName}! Answers will now use live AI models.` });
    } catch (err: any) {
      setAiSaveMsg({ type: 'error', text: 'Failed to configure AI: ' + err.message });
    } finally {
      setIsSavingAi(false);
    }
  };

  const handleResetAiConfig = async () => {
    setIsSavingAi(true);
    try {
      const res = await assistantApi.configureAi({ apiKey: '' });
      setAiStatus(res);
      setCustomApiKey('');
      setAiSaveMsg({ type: 'success', text: 'Reset to DigitalHelper Local Intelligent Engine.' });
    } catch (err: any) {
      setAiSaveMsg({ type: 'error', text: 'Could not reset: ' + err.message });
    } finally {
      setIsSavingAi(false);
    }
  };

  if (!isOpen) return null;

  const languages = [
    { code: 'en', name: 'English' },
    { code: 'ta', name: 'தமிழ் (Tamil)' },
    { code: 'hi', name: 'हिंदी (Hindi)' },
    { code: 'te', name: 'తెలుగు (Telugu)' },
    { code: 'kn', name: 'ಕನ್ನಡ (Kannada)' },
    { code: 'ml', name: 'മലയാളം (Malayalam)' },
    { code: 'bn', name: 'বাংলা (Bengali)' },
  ];

  const handleLanguageChange = (lang: string) => {
    updatePreferences({ preferredLanguage: lang });
  };

  const handleDeleteData = async () => {
    if (!deleteConfirm) {
      setDeleteConfirm(true);
      return;
    }

    setIsDeleting(true);
    try {
      await deleteAccount();
      alert('Your account and all associated data have been permanently removed.');
      onClose();
    } catch (err: any) {
      alert('Could not delete data: ' + err.message);
    } finally {
      setIsDeleting(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 bg-black/60 backdrop-blur-sm flex items-center justify-center p-3 sm:p-4 overflow-y-auto">
      <div className="bg-white dark:bg-slate-900 rounded-3xl border-2 border-slate-300 dark:border-slate-800 shadow-2xl max-w-xl w-full p-5 sm:p-7 relative space-y-5 animate-in zoom-in-95 duration-150 my-6">
        <button
          onClick={onClose}
          className="absolute right-4 top-4 p-1.5 rounded-full hover:bg-slate-100 dark:hover:bg-slate-800 text-slate-400 hover:text-slate-600 dark:hover:text-slate-200 transition cursor-pointer"
        >
          <X className="w-5 h-5" />
        </button>

        {/* Modal Header */}
        <div className="flex items-center gap-3">
          <div className="p-2.5 bg-orange-100 dark:bg-slate-800 text-orange-600 dark:text-orange-400 rounded-2xl">
            <Settings className="w-6 h-6" />
          </div>
          <div>
            <h2 className="text-lg sm:text-xl font-black text-slate-950 dark:text-white">
              Assistant & Accessibility Settings
            </h2>
            <p className="text-xs text-slate-500 dark:text-slate-400 font-medium">
              Configure real AI models (Gemini / ChatGPT) and adjust display readability.
            </p>
          </div>
        </div>

        <div className="space-y-4 text-xs">
          {/* AI MODEL CONFIGURATION (ChatGPT / Gemini Accuracy) */}
          <section className="p-3.5 sm:p-4 rounded-2xl bg-orange-50/60 dark:bg-slate-800/80 border-2 border-orange-200 dark:border-slate-700 space-y-3">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2">
                <Sparkles className="w-4 h-4 text-orange-500 shrink-0" />
                <h3 className="font-black text-slate-950 dark:text-white text-xs sm:text-sm">
                  AI Accuracy Engine (Gemini & ChatGPT)
                </h3>
              </div>
              <span className={`text-[10px] font-black px-2 py-0.5 rounded-full border ${
                aiStatus?.configured
                  ? 'bg-emerald-100 text-emerald-900 border-emerald-300 dark:bg-emerald-950 dark:text-emerald-300'
                  : 'bg-slate-200 text-slate-800 border-slate-300 dark:bg-slate-700 dark:text-slate-300'
              }`}>
                {aiStatus?.configured ? 'Live LLM Active' : 'Offline Engine'}
              </span>
            </div>

            <p className="text-[11px] text-slate-600 dark:text-slate-300 leading-relaxed font-medium">
              Connect <strong>Google Gemini (Free)</strong> or <strong>OpenAI ChatGPT</strong> to answer any question with the highest accuracy, just like ChatGPT and Gemini.
            </p>

            {/* Provider Selection */}
            <div className="grid grid-cols-3 gap-2">
              {[
                { id: 'gemini', label: 'Google Gemini', note: 'Free & Fast (Recommended)' },
                { id: 'openai', label: 'ChatGPT (OpenAI)', note: 'GPT-4o Mini' },
                { id: 'groq', label: 'Groq (Llama-3.3)', note: 'Ultra-fast' },
              ].map((p) => (
                <button
                  key={p.id}
                  type="button"
                  onClick={() => setSelectedProvider(p.id as any)}
                  className={`p-2 rounded-xl border-2 text-left font-bold transition cursor-pointer flex flex-col justify-between h-14 ${
                    selectedProvider === p.id
                      ? 'border-orange-500 bg-white dark:bg-slate-900 text-orange-600 dark:text-orange-400 shadow-xs'
                      : 'border-slate-200 dark:border-slate-700 bg-white/70 dark:bg-slate-900/50 text-slate-700 dark:text-slate-300'
                  }`}
                >
                  <span className="text-xs truncate">{p.label}</span>
                  <span className="text-[9px] text-slate-400 block font-normal">{p.note}</span>
                </button>
              ))}
            </div>

            {/* Key Input Form */}
            <form onSubmit={handleSaveAiConfig} className="space-y-2 pt-1">
              <div className="space-y-1">
                <div className="flex items-center justify-between text-[11px]">
                  <label className="font-bold text-slate-800 dark:text-slate-200 flex items-center gap-1">
                    <Key className="w-3 h-3 text-orange-500" />
                    <span>Enter {selectedProvider === 'gemini' ? 'Gemini' : selectedProvider === 'openai' ? 'OpenAI' : 'Groq'} API Key:</span>
                  </label>
                  {selectedProvider === 'gemini' && (
                    <a
                      href="https://aistudio.google.com/app/apikey"
                      target="_blank"
                      rel="noopener noreferrer"
                      className="text-orange-600 dark:text-orange-400 font-bold hover:underline flex items-center gap-0.5"
                    >
                      <span>Get Free Gemini Key</span>
                      <ExternalLink className="w-3 h-3" />
                    </a>
                  )}
                </div>
                <input
                  type="password"
                  value={customApiKey}
                  onChange={(e) => setCustomApiKey(e.target.value)}
                  placeholder={
                    selectedProvider === 'gemini'
                      ? 'Paste Gemini API Key (starts with AIzaSy...)'
                      : selectedProvider === 'openai'
                      ? 'Paste OpenAI Key (starts with sk-...)'
                      : 'Paste Groq Key (starts with gsk_...)'
                  }
                  className="w-full px-3 py-2 rounded-xl border-2 border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-900 text-slate-900 dark:text-white placeholder-slate-400 text-xs font-mono focus:outline-none focus:border-orange-500"
                />
              </div>

              {aiSaveMsg && (
                <div className={`p-2 rounded-xl text-xs font-bold flex items-center gap-1.5 ${
                  aiSaveMsg.type === 'success'
                    ? 'bg-emerald-100 text-emerald-900 dark:bg-emerald-950 dark:text-emerald-300 border border-emerald-300'
                    : 'bg-rose-100 text-rose-900 dark:bg-rose-950 dark:text-rose-300 border border-rose-300'
                }`}>
                  <CheckCircle2 className="w-3.5 h-3.5 shrink-0" />
                  <span>{aiSaveMsg.text}</span>
                </div>
              )}

              <div className="flex items-center gap-2 pt-1">
                <button
                  type="submit"
                  disabled={isSavingAi || !customApiKey.trim()}
                  className="flex-1 bg-orange-500 hover:bg-orange-600 disabled:opacity-40 text-white font-black py-2 rounded-xl text-xs transition cursor-pointer flex items-center justify-center gap-1.5 shadow-xs"
                >
                  {isSavingAi ? <Loader2 className="w-3.5 h-3.5 animate-spin" /> : <Sparkles className="w-3.5 h-3.5" />}
                  <span>Activate {selectedProvider.toUpperCase()} Model</span>
                </button>
                {aiStatus?.configured && (
                  <button
                    type="button"
                    onClick={handleResetAiConfig}
                    disabled={isSavingAi}
                    className="px-3 py-2 rounded-xl border-2 border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-900 text-slate-700 dark:text-slate-300 font-bold hover:bg-slate-100 transition cursor-pointer text-xs"
                    title="Reset to local offline engine"
                  >
                    Reset
                  </button>
                )}
              </div>
            </form>
          </section>

          {/* Preferred Language */}
          <div className="space-y-1.5">
            <label className="font-bold text-slate-700 dark:text-slate-300 flex items-center gap-1.5">
              <Globe className="w-4 h-4 text-orange-500" />
              <span>Primary Helper Language</span>
            </label>
            <select
              value={preferences?.preferredLanguage || 'en'}
              onChange={(e) => handleLanguageChange(e.target.value)}
              className="w-full p-2.5 rounded-xl border-2 border-slate-300 dark:border-slate-700 bg-slate-50 dark:bg-slate-800 text-slate-900 dark:text-white font-bold focus:outline-none focus:border-orange-500"
            >
              {languages.map((l) => (
                <option key={l.code} value={l.code} className="bg-slate-900 text-white">
                  {l.name}
                </option>
              ))}
            </select>
          </div>

          {/* Text Size Accessibility */}
          <div className="space-y-2">
            <label className="font-bold text-slate-700 dark:text-slate-300 flex items-center gap-1.5">
              <Type className="w-4 h-4 text-orange-500" />
              <span>Reading Text Size</span>
            </label>
            <div className="grid grid-cols-2 gap-2">
              {[
                { id: 'normal', label: 'Standard Size (A)' },
                { id: 'large', label: 'Large Size (A+)' },
              ].map((s) => (
                <button
                  key={s.id}
                  type="button"
                  onClick={() => setFontSize(s.id as any)}
                  className={`p-2.5 rounded-xl border-2 text-center font-bold transition cursor-pointer ${
                    fontSize === s.id
                      ? 'border-orange-500 bg-orange-50 dark:bg-slate-800 text-orange-600 dark:text-orange-400 font-black'
                      : 'border-slate-300 dark:border-slate-700 bg-slate-50 dark:bg-slate-800 text-slate-700 dark:text-slate-300'
                  }`}
                >
                  {s.label}
                </button>
              ))}
            </div>
          </div>

          {/* Dark Mode toggle */}
          <div className="flex items-center justify-between p-3 rounded-xl bg-slate-50 dark:bg-slate-800 border-2 border-slate-300 dark:border-slate-700">
            <div className="flex items-center gap-2.5">
              {darkMode ? <Moon className="w-4 h-4 text-orange-400" /> : <Sun className="w-4 h-4 text-amber-500" />}
              <div>
                <span className="font-bold text-slate-800 dark:text-slate-200 block">Dark Theme</span>
                <span className="text-[11px] text-slate-500 dark:text-slate-400">High-contrast stylish dark mode</span>
              </div>
            </div>
            <input
              type="checkbox"
              checked={darkMode}
              onChange={(e) => setDarkMode(!darkMode)}
              className="w-4 h-4 text-orange-500 rounded cursor-pointer accent-orange-500"
            />
          </div>

          {/* Privacy & Account Deletion */}
          {user && (
            <div className="pt-2 border-t border-slate-200 dark:border-slate-800 space-y-2">
              <div className="flex items-center gap-2 text-rose-600 dark:text-rose-400 font-bold">
                <Trash2 className="w-4 h-4" />
                <span>Data Privacy & Right to Be Forgotten</span>
              </div>
              <p className="text-[11px] text-slate-500">
                Permanently deletes your account, conversation history, saved answers, and security logs.
              </p>

              <button
                type="button"
                onClick={handleDeleteData}
                disabled={isDeleting}
                className="w-full py-2 rounded-xl border-2 border-rose-300 dark:border-rose-900 bg-rose-50 dark:bg-rose-950/40 text-rose-700 dark:text-rose-300 font-bold hover:bg-rose-100 transition flex items-center justify-center gap-2 cursor-pointer"
              >
                {isDeleting ? <Loader2 className="w-4 h-4 animate-spin" /> : null}
                <span>
                  {deleteConfirm
                    ? '⚠️ Click Again to Confirm Permanent Deletion'
                    : 'Permanently Delete My Data & Account'}
                </span>
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
