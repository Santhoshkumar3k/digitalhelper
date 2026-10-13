import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import {
  ArrowRight,
  ShieldAlert,
  Sparkles,
  HelpCircle,
  Search,
  Lock,
  LayoutDashboard,
} from 'lucide-react';

interface HomeDashboardProps {
  onNavigate: (tab: string, initialPrompt?: string) => void;
  onOpenSettings?: () => void;
}

export const HomeDashboard: React.FC<HomeDashboardProps> = ({ onNavigate, onOpenSettings }) => {
  const { user, t } = useAuth();
  const [quickQuery, setQuickQuery] = useState('');

  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!quickQuery.trim()) return;
    onNavigate('assistant', quickQuery.trim());
  };

  const sampleQuestions = [
    { text: 'How do I clear WhatsApp storage without losing chats?', tag: 'WhatsApp' },
    { text: 'Why is my phone heating up and how to cool it down?', tag: 'Phone Care' },
    { text: 'Someone asked for an OTP to give me a refund. Is it safe?', tag: 'Safety' },
    { text: 'How to transfer photos from phone to laptop without internet?', tag: 'Storage' },
  ];

  // 6 Everyday Helper Services with Text on LEFT & Real 3D Image on RIGHT
  const publicServices = [
    {
      id: 'assistant',
      title: t.card1Title,
      desc: t.card1Desc,
      badge: 'Step-by-Step AI',
      badgeColor: 'bg-orange-100 text-orange-950 border-orange-300 dark:bg-slate-800 dark:text-orange-300 dark:border-slate-700',
      cardBorder: 'border-2 border-slate-300 dark:border-slate-700 hover:border-orange-500 dark:hover:border-white/50',
      cardBg: 'bg-slate-100 dark:bg-slate-900',
      btnColor: 'text-orange-600 dark:text-orange-400 group-hover:text-orange-700',
      imageSrc: '/assets/images/ai_3d.jpg',
      imageAlt: '3D AI Assistant Robot',
    },
    {
      id: 'screenshot',
      title: t.card2Title,
      desc: t.card2Desc,
      badge: 'Vision Explainer',
      badgeColor: 'bg-rose-100 text-rose-950 border-rose-300 dark:bg-slate-800 dark:text-rose-300 dark:border-slate-700',
      cardBorder: 'border-2 border-slate-300 dark:border-slate-700 hover:border-rose-500 dark:hover:border-white/50',
      cardBg: 'bg-slate-100 dark:bg-slate-900',
      btnColor: 'text-rose-600 dark:text-rose-400 group-hover:text-rose-700',
      imageSrc: '/assets/images/screenshot_3d.jpg',
      imageAlt: '3D Smartphone Error Explainer',
    },
    {
      id: 'translate',
      title: t.card3Title,
      desc: t.card3Desc,
      badge: 'Simple Language',
      badgeColor: 'bg-emerald-100 text-emerald-950 border-emerald-300 dark:bg-slate-800 dark:text-emerald-300 dark:border-slate-700',
      cardBorder: 'border-2 border-slate-300 dark:border-slate-700 hover:border-emerald-500 dark:hover:border-white/50',
      cardBg: 'bg-slate-100 dark:bg-slate-900',
      btnColor: 'text-emerald-600 dark:text-emerald-400 group-hover:text-emerald-700',
      imageSrc: '/assets/images/translate_3d.jpg',
      imageAlt: '3D Multilingual Translation Globe',
    },
    {
      id: 'scam',
      title: t.card4Title,
      desc: t.card4Desc,
      badge: 'Anti-Fraud Alert',
      badgeColor: 'bg-amber-100 text-amber-950 border-amber-300 dark:bg-slate-800 dark:text-amber-300 dark:border-slate-700',
      cardBorder: 'border-2 border-slate-300 dark:border-slate-700 hover:border-amber-500 dark:hover:border-white/50',
      cardBg: 'bg-slate-100 dark:bg-slate-900',
      btnColor: 'text-amber-600 dark:text-amber-400 group-hover:text-amber-700',
      imageSrc: '/assets/images/scam_3d.jpg',
      imageAlt: '3D Cybersecurity Shield',
    },
    {
      id: 'forms',
      title: t.card5Title,
      desc: t.card5Desc,
      badge: 'Forms & KYC',
      badgeColor: 'bg-sky-100 text-sky-950 border-sky-300 dark:bg-slate-800 dark:text-sky-300 dark:border-slate-700',
      cardBorder: 'border-2 border-slate-300 dark:border-slate-700 hover:border-sky-500 dark:hover:border-white/50',
      cardBg: 'bg-slate-100 dark:bg-slate-900',
      btnColor: 'text-sky-600 dark:text-sky-400 group-hover:text-sky-700',
      imageSrc: '/assets/images/forms_3d.jpg',
      imageAlt: '3D Government Documents & Checklist',
    },
    {
      id: 'saved',
      title: t.card6Title,
      desc: t.card6Desc,
      badge: 'Personal Vault',
      badgeColor: 'bg-purple-100 text-purple-950 border-purple-300 dark:bg-slate-800 dark:text-purple-300 dark:border-slate-700',
      cardBorder: 'border-2 border-slate-300 dark:border-slate-700 hover:border-purple-500 dark:hover:border-white/50',
      cardBg: 'bg-slate-100 dark:bg-slate-900',
      btnColor: 'text-purple-600 dark:text-purple-400 group-hover:text-purple-700',
      imageSrc: '/assets/images/vault_3d.jpg',
      imageAlt: '3D Secure Vault Safe Box',
    },
  ];

  return (
    <div className="w-full max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-2.5 sm:py-4 space-y-4">
      {/* Hero Welcome Banner - Visible borders, Distinct grey shade in bright mode, Combined with white in dark mode */}
      <section className="bg-slate-100 dark:bg-slate-900 rounded-2xl border-2 border-slate-300 dark:border-slate-700 p-3 sm:p-4 shadow-xs flex flex-col md:flex-row items-center justify-between gap-3 transition-colors">
        <div className="space-y-0.5 text-center md:text-left flex-1">
          <div className="inline-flex items-center gap-1 bg-orange-100 dark:bg-slate-800 text-orange-950 dark:text-orange-300 px-2 py-0.5 rounded-full text-[10px] font-black border border-orange-300 dark:border-slate-700">
            <Sparkles className="w-3 h-3 text-orange-500" />
            <span>DigitalHelper — Technology Made Simple</span>
          </div>
          <h1 className="text-lg sm:text-xl font-black text-slate-950 dark:text-white tracking-tight">
            {t.greetingTitle}
          </h1>
          <p className="text-xs text-slate-700 dark:text-slate-300 font-semibold max-w-lg">
            {t.greetingSubtitle}
          </p>
        </div>

        {/* Clean, Compact Question Input Box */}
        <form
          onSubmit={handleSearchSubmit}
          className="w-full md:w-auto md:min-w-[320px] flex items-center bg-white dark:bg-slate-800 border-2 border-slate-300 dark:border-slate-700 rounded-xl p-1 focus-within:border-orange-500 shadow-xs"
        >
          <Search className="w-4 h-4 text-orange-500 ml-2 shrink-0" />
          <input
            type="text"
            value={quickQuery}
            onChange={(e) => setQuickQuery(e.target.value)}
            placeholder={t.searchPlaceholder}
            className="w-full px-2 py-1 text-xs bg-transparent text-slate-950 dark:text-white placeholder-slate-400 dark:placeholder-slate-500 focus:outline-none font-semibold truncate"
          />
          <button
            type="submit"
            className="shrink-0 bg-orange-500 hover:bg-orange-600 text-white font-black px-3.5 py-1 rounded-lg text-xs transition cursor-pointer shadow-xs"
          >
            {t.askButton}
          </button>
        </form>
      </section>

      {/* 6 Everyday Digital Helper Tools Grid - Text on LEFT & Real 3D Image on RIGHT */}
      <section className="space-y-3">
        {/* Section Title only */}
        <div className="flex items-center justify-between border-b-2 border-slate-200 dark:border-slate-800 pb-2">
          <h2 className="text-base sm:text-lg font-black text-slate-950 dark:text-white flex items-center gap-2">
            <span className="w-2.5 h-2.5 rounded-full bg-orange-500 inline-block"></span>
            {t.servicesTitle}
          </h2>
        </div>

        {/* 6 Core Helper Cards: Text on LEFT, Real 3D Image on RIGHT */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-3.5">
          {publicServices.map((card) => (
            <div
              key={card.id}
              onClick={() => onNavigate(card.id)}
              className={`rounded-2xl p-4 ${card.cardBorder} ${card.cardBg} shadow-xs hover:shadow-lg hover:-translate-y-0.5 transition-all duration-150 cursor-pointer flex flex-row items-center justify-between gap-3.5 group min-h-[180px]`}
            >
              {/* Left Side: Texts (Badge, Title, Description, Action Button) */}
              <div className="flex-1 flex flex-col justify-between h-full min-w-0 pr-1 space-y-1.5">
                <div>
                  <span className={`inline-block text-[10px] font-black uppercase tracking-wider px-2 py-0.5 rounded-full border ${card.badgeColor} mb-1 truncate max-w-full`}>
                    {card.badge}
                  </span>
                  <h3
                    className="text-sm sm:text-base font-black text-slate-950 dark:text-white line-clamp-1 group-hover:text-orange-500 transition-colors"
                    title={card.title}
                  >
                    {card.title}
                  </h3>
                  <p
                    className="text-xs text-slate-700 dark:text-slate-300 leading-relaxed font-semibold line-clamp-2 mt-0.5"
                    title={card.desc}
                  >
                    {card.desc}
                  </p>
                </div>

                <div className={`pt-1.5 flex items-center gap-1.5 text-xs font-black ${card.btnColor}`}>
                  <span>{t.openTool}</span>
                  <ArrowRight className="w-3.5 h-3.5 group-hover:translate-x-1 transition-transform" />
                </div>
              </div>

              {/* Right Side: Real 3D Image */}
              <div className="shrink-0">
                <img
                  src={card.imageSrc}
                  alt={card.imageAlt}
                  loading="lazy"
                  className="w-20 h-20 sm:w-22 sm:h-22 object-cover rounded-2xl shadow-md border-2 border-slate-200 dark:border-slate-700 group-hover:scale-105 transition-transform"
                />
              </div>
            </div>
          ))}
        </div>
      </section>

      {/* ADMIN ROOM: Strictly shown ONLY to Admin Users */}
      {user?.role === 'ROLE_ADMIN' && (
        <section className="bg-rose-50/80 dark:bg-slate-900 border-2 border-rose-300 dark:border-slate-800 rounded-2xl p-4 shadow-xs">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
            <div className="flex items-center gap-3">
              <div className="w-9 h-9 rounded-xl bg-rose-600 text-white flex items-center justify-center shadow-sm shrink-0">
                <Lock className="w-4 h-4" />
              </div>
              <div>
                <div className="flex items-center gap-2">
                  <h3 className="text-sm sm:text-base font-black text-rose-950 dark:text-white">
                    Administrator Room
                  </h3>
                  <span className="text-[10px] font-black bg-rose-200 dark:bg-slate-800 text-rose-900 dark:text-rose-300 px-2 py-0.5 rounded-full border border-rose-300 dark:border-slate-700">
                    Admin Access Only
                  </span>
                </div>
                <p className="text-xs text-rose-900 dark:text-slate-300 font-semibold">
                  You are logged in as Administrator. Review system analytics, user ratings, and security telemetry.
                </p>
              </div>
            </div>
            <button
              onClick={() => onNavigate('admin')}
              className="shrink-0 bg-rose-600 hover:bg-rose-700 text-white font-black text-xs px-4 py-2 rounded-xl shadow-xs transition flex items-center gap-1.5 cursor-pointer"
            >
              <LayoutDashboard className="w-4 h-4" />
              <span>Open Admin Dashboard</span>
            </button>
          </div>
        </section>
      )}

      {/* Popular Starter Questions for Elders and Beginners */}
      <section className="bg-slate-100 dark:bg-slate-900 rounded-2xl border-2 border-slate-300 dark:border-slate-700 p-3.5 shadow-xs space-y-2">
        <div className="flex items-center gap-2 text-xs font-black uppercase tracking-wider text-slate-900 dark:text-white">
          <HelpCircle className="w-4 h-4 text-orange-500" />
          <span>{t.quickQuestionsTitle}</span>
        </div>
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-2.5">
          {sampleQuestions.map((q, idx) => (
            <button
              key={idx}
              onClick={() => onNavigate('assistant', q.text)}
              className="text-left text-xs bg-white dark:bg-slate-800 hover:bg-orange-50 dark:hover:bg-slate-700 text-slate-950 dark:text-white hover:text-orange-600 dark:hover:text-orange-400 p-2.5 rounded-xl border-2 border-slate-300 dark:border-slate-700 hover:border-orange-400 transition shadow-2xs flex flex-col justify-between gap-1 cursor-pointer font-bold h-20"
            >
              <span className="line-clamp-2">{q.text}</span>
              <span className="self-start text-[10px] bg-orange-100 dark:bg-slate-900 text-orange-950 dark:text-orange-300 px-2 py-0.5 rounded-full font-black border border-orange-300 dark:border-slate-700">
                {q.tag}
              </span>
            </button>
          ))}
        </div>
      </section>

      {/* Safety Notice Reminder Banner */}
      <section className="bg-amber-50 dark:bg-slate-900 border-2 border-amber-300 dark:border-amber-800/80 rounded-2xl p-3 sm:p-3.5 flex flex-col sm:flex-row items-center justify-between gap-3 shadow-2xs">
        <div className="flex items-center gap-3">
          <div className="p-2 bg-amber-200 dark:bg-slate-800 text-amber-950 dark:text-amber-300 rounded-xl shrink-0">
            <ShieldAlert className="w-4 h-4" />
          </div>
          <div className="space-y-0.5">
            <h4 className="text-xs sm:text-sm font-black text-amber-950 dark:text-white">
              Golden Safety Rule for Everyone:
            </h4>
            <p className="text-xs text-amber-950 dark:text-slate-300 font-semibold leading-relaxed">
              Never share your OTP or UPI PIN. Banks and government offices will <strong>never call or message</strong> asking for them.
            </p>
          </div>
        </div>
        <button
          onClick={() => onNavigate('scam')}
          className="shrink-0 bg-amber-600 hover:bg-amber-700 text-white font-black text-xs px-3.5 py-1.5 rounded-xl shadow-xs transition cursor-pointer"
        >
          Check Suspicious Message
        </button>
      </section>
    </div>
  );
};
