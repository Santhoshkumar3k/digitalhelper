import React, { useState, useEffect } from 'react';
import { adminApi, healthApi } from '../services/api';
import { AdminStats, Feedback } from '../types';
import {
  LayoutDashboard,
  Users,
  MessageSquare,
  ShieldAlert,
  ThumbsUp,
  Star,
  Activity,
  Server,
  RefreshCw,
  Loader2,
  CheckCircle2,
  AlertTriangle,
} from 'lucide-react';

export const AdminDashboardView: React.FC = () => {
  const [stats, setStats] = useState<AdminStats | null>(null);
  const [feedbackList, setFeedbackList] = useState<Feedback[]>([]);
  const [health, setHealth] = useState<any>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);

  useEffect(() => {
    loadAdminData();
  }, []);

  const loadAdminData = async () => {
    setIsLoading(true);
    try {
      const [s, f, h] = await Promise.all([
        adminApi.getStats(),
        adminApi.getAllFeedback(),
        healthApi.getHealth().catch(() => null),
      ]);
      setStats(s);
      setFeedbackList(f);
      setHealth(h);
    } catch (err: any) {
      alert('Error fetching admin data: ' + err.message);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
      {/* Header */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div>
          <div className="inline-flex items-center gap-1.5 bg-purple-100 dark:bg-purple-950 text-purple-700 dark:text-purple-300 px-3 py-1 rounded-full text-xs font-bold border border-purple-200 dark:border-purple-800 mb-2">
            <LayoutDashboard className="w-3.5 h-3.5" />
            <span>Admin Health & Feedback Hub</span>
          </div>
          <h1 className="text-3xl font-extrabold text-slate-900 dark:text-white">
            Application Operations Dashboard
          </h1>
          <p className="text-sm text-slate-500">
            Aggregate health metrics, scam detection alerts, and privacy-safe user ratings.
          </p>
        </div>

        <button
          onClick={loadAdminData}
          disabled={isLoading}
          className="flex items-center gap-2 bg-slate-800 hover:bg-slate-700 text-white px-4 py-2 rounded-xl text-xs font-bold transition"
        >
          <RefreshCw className={`w-4 h-4 ${isLoading ? 'animate-spin' : ''}`} />
          <span>Refresh Metrics</span>
        </button>
      </div>

      {isLoading ? (
        <div className="p-16 flex flex-col items-center justify-center text-slate-400 space-y-2">
          <Loader2 className="w-8 h-8 animate-spin text-primary-500" />
          <p className="text-xs">Loading aggregate statistics...</p>
        </div>
      ) : stats ? (
        <>
          {/* Key Metric Stat Cards */}
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            <div className="bg-white dark:bg-slate-900 p-5 rounded-2xl border border-slate-200 dark:border-slate-800 shadow-sm space-y-1">
              <span className="text-xs font-bold uppercase tracking-wider text-slate-400 flex items-center gap-1.5">
                <Users className="w-4 h-4 text-blue-500" /> Registered Users
              </span>
              <p className="text-2xl font-black text-slate-900 dark:text-white">{stats.totalUsers}</p>
              <span className="text-[11px] text-slate-400">Total accounts</span>
            </div>

            <div className="bg-white dark:bg-slate-900 p-5 rounded-2xl border border-slate-200 dark:border-slate-800 shadow-sm space-y-1">
              <span className="text-xs font-bold uppercase tracking-wider text-slate-400 flex items-center gap-1.5">
                <MessageSquare className="w-4 h-4 text-indigo-500" /> Conversations
              </span>
              <p className="text-2xl font-black text-slate-900 dark:text-white">{stats.totalConversations}</p>
              <span className="text-[11px] text-slate-400">{stats.totalMessages} assistant answers</span>
            </div>

            <div className="bg-white dark:bg-slate-900 p-5 rounded-2xl border border-slate-200 dark:border-slate-800 shadow-sm space-y-1">
              <span className="text-xs font-bold uppercase tracking-wider text-slate-400 flex items-center gap-1.5">
                <ShieldAlert className="w-4 h-4 text-rose-500" /> High-Risk Scams
              </span>
              <p className="text-2xl font-black text-rose-600 dark:text-rose-400">{stats.highRiskScamsDetected}</p>
              <span className="text-[11px] text-slate-400">Out of {stats.totalScamChecks} checks</span>
            </div>

            <div className="bg-white dark:bg-slate-900 p-5 rounded-2xl border border-slate-200 dark:border-slate-800 shadow-sm space-y-1">
              <span className="text-xs font-bold uppercase tracking-wider text-slate-400 flex items-center gap-1.5">
                <ThumbsUp className="w-4 h-4 text-emerald-500" /> Satisfaction
              </span>
              <p className="text-2xl font-black text-emerald-600 dark:text-emerald-400">
                {Math.round(stats.positiveFeedbackPercent)}%
              </p>
              <span className="text-[11px] text-slate-400">{stats.averageRating} / 5.0 Star Rating</span>
            </div>
          </div>

          {/* System Health & AI Provider Status */}
          <div className="bg-white dark:bg-slate-900 p-6 rounded-2xl border border-slate-200 dark:border-slate-800 shadow-sm space-y-4">
            <h3 className="font-bold text-sm text-slate-900 dark:text-white flex items-center gap-2">
              <Server className="w-4 h-4 text-primary-500" />
              <span>System & AI Infrastructure Health</span>
            </h3>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-4 text-xs">
              <div className="p-3.5 rounded-xl bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700">
                <span className="text-slate-400 block mb-1">Backend REST Service:</span>
                <span className="font-bold text-emerald-500 flex items-center gap-1">
                  <CheckCircle2 className="w-4 h-4" /> Spring Boot 3.2.5 (Port 8080)
                </span>
              </div>

              <div className="p-3.5 rounded-xl bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700">
                <span className="text-slate-400 block mb-1">Active AI Engine:</span>
                <span className="font-bold text-slate-800 dark:text-slate-200">
                  {stats.aiProvider}
                </span>
              </div>

              <div className="p-3.5 rounded-xl bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700">
                <span className="text-slate-400 block mb-1">Database & Persistence:</span>
                <span className="font-bold text-slate-800 dark:text-slate-200">
                  MySQL / In-Memory H2 Dual Compatible
                </span>
              </div>
            </div>
          </div>

          {/* User Feedback Log (Privacy Safe) */}
          <div className="bg-white dark:bg-slate-900 rounded-2xl border border-slate-200 dark:border-slate-800 p-6 shadow-sm space-y-4">
            <div className="flex items-center justify-between">
              <div>
                <h3 className="font-bold text-sm text-slate-900 dark:text-white flex items-center gap-2">
                  <Star className="w-4 h-4 text-amber-500" />
                  <span>Recent User Ratings & Feedback</span>
                </h3>
                <p className="text-xs text-slate-400 mt-0.5">
                  Private conversations are kept confidential. Only user rating scores and helpfulness flags are surfaced.
                </p>
              </div>
              <span className="text-xs font-semibold bg-slate-100 dark:bg-slate-800 px-2.5 py-1 rounded-full text-slate-500">
                {feedbackList.length} reviews
              </span>
            </div>

            <div className="divide-y divide-slate-100 dark:divide-slate-800 overflow-x-auto">
              {feedbackList.length === 0 ? (
                <div className="py-8 text-center text-slate-400 text-xs">
                  No feedback submitted yet.
                </div>
              ) : (
                feedbackList.map((f) => (
                  <div key={f.id} className="py-3 flex items-center justify-between gap-4 text-xs">
                    <div className="space-y-1 min-w-0">
                      <div className="flex items-center gap-2">
                        <span className={`px-2 py-0.5 rounded text-[10px] font-bold ${
                          f.helpful ? 'bg-emerald-100 text-emerald-800 dark:bg-emerald-950 dark:text-emerald-300' : 'bg-rose-100 text-rose-800 dark:bg-rose-950 dark:text-rose-300'
                        }`}>
                          {f.helpful ? 'Helpful' : 'Needs Work'}
                        </span>
                        <span className="font-bold text-slate-700 dark:text-slate-300">{f.feature}</span>
                        <span className="text-slate-400 text-[10px]">• {new Date(f.createdAt).toLocaleDateString()}</span>
                      </div>
                      <p className="text-slate-600 dark:text-slate-400 truncate">
                        "{f.comments || 'No comment provided'}"
                      </p>
                    </div>

                    <div className="flex items-center gap-1 text-amber-400 shrink-0">
                      {Array.from({ length: f.rating }).map((_, i) => (
                        <Star key={i} className="w-3.5 h-3.5 fill-current" />
                      ))}
                    </div>
                  </div>
                ))
              )}
            </div>
          </div>
        </>
      ) : null}
    </div>
  );
};
