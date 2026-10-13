import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { savedAnswerApi } from '../services/api';
import { SavedAnswer } from '../types';
import {
  Bookmark,
  BookmarkCheck,
  Search,
  Star,
  Trash2,
  Copy,
  Check,
  Volume2,
  VolumeX,
  Tag,
  Folder,
  Loader2,
  Sparkles,
} from 'lucide-react';

export const SavedAnswersView: React.FC = () => {
  const { user } = useAuth();
  const [answers, setAnswers] = useState<SavedAnswer[]>([]);
  const [activeCategory, setActiveCategory] = useState<string>('All');
  const [searchQuery, setSearchQuery] = useState<string>('');
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [copiedId, setCopiedId] = useState<number | null>(null);
  const [speakingId, setSpeakingId] = useState<number | null>(null);

  const categories = [
    'All',
    'Banking & Security',
    'Device Help',
    'WhatsApp & Social',
    'Official Forms',
    'General',
  ];

  useEffect(() => {
    loadSavedAnswers();
  }, [activeCategory, user]);

  const loadSavedAnswers = async () => {
    if (!user) {
      setIsLoading(false);
      return;
    }
    setIsLoading(true);
    try {
      const data = await savedAnswerApi.getAll(activeCategory, searchQuery);
      setAnswers(data);
    } catch {
      // ignore
    } finally {
      setIsLoading(false);
    }
  };

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    loadSavedAnswers();
  };

  const handleToggleFavorite = async (id: number) => {
    try {
      const updated = await savedAnswerApi.toggleFavorite(id);
      setAnswers((prev) => prev.map((a) => (a.id === id ? updated : a)));
    } catch {
      // ignore
    }
  };

  const handleDelete = async (id: number) => {
    if (!confirm('Are you sure you want to remove this bookmark?')) return;
    try {
      await savedAnswerApi.delete(id);
      setAnswers((prev) => prev.filter((a) => a.id !== id));
    } catch {
      // ignore
    }
  };

  const handleCopy = (id: number, text: string) => {
    navigator.clipboard.writeText(text);
    setCopiedId(id);
    setTimeout(() => setCopiedId(null), 2000);
  };

  const toggleTTS = (id: number, text: string) => {
    if (!('speechSynthesis' in window)) return;
    if (speakingId === id) {
      window.speechSynthesis.cancel();
      setSpeakingId(null);
      return;
    }
    window.speechSynthesis.cancel();
    const cleanText = text.replace(/[*#_`]/g, '');
    const utterance = new SpeechSynthesisUtterance(cleanText);
    utterance.rate = 0.95;
    utterance.onend = () => setSpeakingId(null);
    utterance.onerror = () => setSpeakingId(null);
    window.speechSynthesis.speak(utterance);
    setSpeakingId(id);
  };

  return (
    <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
      {/* Header */}
      <div className="max-w-3xl space-y-2">
        <div className="inline-flex items-center gap-1.5 bg-indigo-100 dark:bg-indigo-950 text-indigo-700 dark:text-indigo-300 px-3 py-1 rounded-full text-xs font-bold border border-indigo-200 dark:border-indigo-800">
          <BookmarkCheck className="w-3.5 h-3.5" />
          <span>Personal Knowledge Vault</span>
        </div>
        <h1 className="text-3xl font-extrabold text-slate-900 dark:text-white">
          Saved Answers & Bookmarks
        </h1>
        <p className="text-sm text-slate-600 dark:text-slate-300">
          Revisit helpful solutions, PIN rules, and phone troubleshooting steps you've saved.
        </p>
      </div>

      {/* Category Pills & Search Bar */}
      <div className="flex flex-col sm:flex-row items-center justify-between gap-4 bg-white dark:bg-slate-900 p-4 rounded-2xl border border-slate-200 dark:border-slate-800 shadow-sm">
        {/* Categories */}
        <div className="flex flex-wrap gap-1.5 w-full sm:w-auto">
          {categories.map((cat) => (
            <button
              key={cat}
              onClick={() => setActiveCategory(cat)}
              className={`px-3 py-1.5 rounded-xl text-xs font-bold transition ${
                activeCategory === cat
                  ? 'bg-primary-600 text-white shadow-sm'
                  : 'bg-slate-100 dark:bg-slate-800 text-slate-600 dark:text-slate-300 hover:bg-slate-200'
              }`}
            >
              {cat}
            </button>
          ))}
        </div>

        {/* Search */}
        <form onSubmit={handleSearch} className="flex items-center gap-2 w-full sm:w-72">
          <div className="relative w-full">
            <Search className="w-4 h-4 text-slate-400 absolute left-3 top-2.5" />
            <input
              type="text"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              placeholder="Search saved bookmarks..."
              className="w-full pl-9 pr-3 py-2 text-xs rounded-xl bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 text-slate-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-primary-500"
            />
          </div>
          <button
            type="submit"
            className="px-3 py-2 bg-slate-800 text-white rounded-xl text-xs font-bold hover:bg-slate-700"
          >
            Filter
          </button>
        </form>
      </div>

      {/* List of Saved Answers */}
      {isLoading ? (
        <div className="p-16 flex flex-col items-center justify-center text-slate-400 space-y-2">
          <Loader2 className="w-8 h-8 animate-spin text-primary-500" />
          <p className="text-xs">Loading your saved answers...</p>
        </div>
      ) : !user ? (
        <div className="bg-slate-50 dark:bg-slate-900/40 border-2 border-dashed border-slate-200 dark:border-slate-800 rounded-2xl p-12 text-center text-slate-500 space-y-3">
          <Bookmark className="w-12 h-12 text-slate-400 mx-auto" />
          <h3 className="font-bold text-slate-800 dark:text-slate-200 text-sm">
            Sign in to view and organize your saved answers
          </h3>
          <p className="text-xs max-w-sm mx-auto">
            Your bookmarks are safely stored and synced across devices with your account.
          </p>
        </div>
      ) : answers.length === 0 ? (
        <div className="bg-slate-50 dark:bg-slate-900/40 border-2 border-dashed border-slate-200 dark:border-slate-800 rounded-2xl p-12 text-center text-slate-500 space-y-3">
          <Bookmark className="w-12 h-12 text-slate-400 mx-auto" />
          <h3 className="font-bold text-slate-800 dark:text-slate-200 text-sm">
            No saved answers in this category
          </h3>
          <p className="text-xs max-w-sm mx-auto">
            When you ask a question in the AI Assistant, tap the bookmark icon on any answer to save it here for future reference.
          </p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
          {answers.map((item) => (
            <div
              key={item.id}
              className="bg-white dark:bg-slate-900 rounded-2xl border border-slate-200 dark:border-slate-800 p-5 shadow-sm space-y-3 flex flex-col justify-between"
            >
              <div>
                <div className="flex items-center justify-between gap-2 mb-2">
                  <span className="text-[10px] font-bold uppercase tracking-wider bg-slate-100 dark:bg-slate-800 text-slate-600 dark:text-slate-400 px-2.5 py-0.5 rounded-full border border-slate-200 dark:border-slate-700">
                    {item.category}
                  </span>

                  <div className="flex items-center gap-1">
                    <button
                      onClick={() => handleToggleFavorite(item.id)}
                      className={`p-1.5 rounded-lg transition ${
                        item.isFavorite
                          ? 'text-amber-400 hover:text-amber-500'
                          : 'text-slate-400 hover:text-amber-400'
                      }`}
                      title={item.isFavorite ? 'Favorited' : 'Add to favorites'}
                    >
                      <Star className={`w-4 h-4 ${item.isFavorite ? 'fill-current' : ''}`} />
                    </button>

                    <button
                      onClick={() => handleDelete(item.id)}
                      className="p-1.5 rounded-lg text-slate-400 hover:text-rose-500 transition"
                      title="Delete bookmark"
                    >
                      <Trash2 className="w-4 h-4" />
                    </button>
                  </div>
                </div>

                <h3 className="font-bold text-sm sm:text-base text-slate-900 dark:text-white">
                  {item.title}
                </h3>

                {item.originalQuestion && (
                  <p className="text-xs text-slate-500 italic">
                    Q: "{item.originalQuestion}"
                  </p>
                )}

                <div className="text-xs text-slate-700 dark:text-slate-300 whitespace-pre-wrap leading-relaxed mt-2 pt-2 border-t border-slate-100 dark:border-slate-800">
                  {item.content}
                </div>
              </div>

              {/* Actions Footer */}
              <div className="flex items-center justify-between pt-3 border-t border-slate-100 dark:border-slate-800 text-xs text-slate-400">
                <span className="text-[11px]">
                  Saved {new Date(item.createdAt).toLocaleDateString()}
                </span>

                <div className="flex items-center gap-1">
                  <button
                    onClick={() => toggleTTS(item.id, item.content)}
                    className="p-1.5 rounded-lg hover:bg-slate-100 dark:hover:bg-slate-800 text-slate-500 hover:text-primary-600 transition"
                    title="Read aloud"
                  >
                    {speakingId === item.id ? <VolumeX className="w-4 h-4 text-primary-500" /> : <Volume2 className="w-4 h-4" />}
                  </button>

                  <button
                    onClick={() => handleCopy(item.id, item.content)}
                    className="p-1.5 rounded-lg hover:bg-slate-100 dark:hover:bg-slate-800 text-slate-500 hover:text-primary-600 transition"
                    title="Copy content"
                  >
                    {copiedId === item.id ? <Check className="w-4 h-4 text-emerald-500" /> : <Copy className="w-4 h-4" />}
                  </button>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};
