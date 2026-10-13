import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import {
  Sparkles,
  LogIn,
  LogOut,
  Settings,
  LayoutDashboard,
  ChevronDown,
  Globe,
  Home,
} from 'lucide-react';

interface NavbarProps {
  currentTab: string;
  setCurrentTab: (tab: string) => void;
  onOpenAuth: () => void;
  onOpenSettings: () => void;
}

export const Navbar: React.FC<NavbarProps> = ({
  currentTab,
  setCurrentTab,
  onOpenAuth,
  onOpenSettings,
}) => {
  const { user, logout, t, currentLanguage, setLanguage } = useAuth();
  const [isUserMenuOpen, setIsUserMenuOpen] = useState(false);

  const handleGoHome = () => {
    setCurrentTab('home');
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  return (
    <nav className="w-full bg-white dark:bg-slate-900 border-b-2 border-slate-300 dark:border-slate-800 sticky top-0 z-40 shadow-xs transition-colors px-4 sm:px-8">
      <div className="w-full flex items-center justify-between h-16 sm:h-18">
        {/* Top-Left Corner: Brand Logo & App Name */}
        <div
          className="flex items-center gap-2.5 sm:gap-3 cursor-pointer group shrink-0"
          onClick={handleGoHome}
          title="DigitalHelper — Go to Home"
        >
          <div className="w-10 h-10 sm:w-11 sm:h-11 rounded-2xl bg-gradient-to-tr from-orange-500 via-rose-500 to-amber-500 flex items-center justify-center text-white shadow-md shadow-orange-500/25 group-hover:scale-105 transition-transform shrink-0">
            <Sparkles className="w-5 h-5 sm:w-6 sm:h-6" />
          </div>
          <div className="select-none">
            <div className="flex items-center gap-2">
              <span className="text-xl sm:text-2xl font-black text-slate-950 dark:text-white tracking-tight">
                Digital<span className="text-orange-500">Helper</span>
              </span>
              <span className="text-[10px] font-black bg-orange-100 dark:bg-slate-800 text-orange-950 dark:text-orange-300 px-2 py-0.5 rounded-full border border-orange-300 dark:border-slate-700">
                Easy Tech
              </span>
            </div>
            <p className="text-[11px] text-slate-500 dark:text-slate-400 font-bold hidden sm:block">
              Technology Made Simple for Everyone
            </p>
          </div>
        </div>

        {/* Center: Home Button when on secondary pages */}
        {currentTab !== 'home' && (
          <button
            onClick={handleGoHome}
            className="flex items-center gap-1.5 px-3.5 py-1.5 rounded-xl bg-orange-50 dark:bg-slate-800 text-orange-700 dark:text-orange-300 border-2 border-orange-200 dark:border-slate-700 font-black text-xs hover:bg-orange-100 dark:hover:bg-slate-700 transition cursor-pointer shadow-2xs"
            title="Return to Home Dashboard"
          >
            <Home className="w-4 h-4" />
            <span className="hidden sm:inline">Back to Home</span>
          </button>
        )}

        {/* Top-Right Corner: Language Dropdown and Sign In */}
        <div className="flex items-center gap-2 sm:gap-3 shrink-0">
          {/* Language Selector Dropdown */}
          <div className="relative flex items-center bg-white dark:bg-slate-800 border-2 border-slate-300 dark:border-slate-700 rounded-xl px-2.5 sm:px-3 py-1 hover:border-orange-500 transition shadow-2xs">
            <Globe className="w-4 h-4 text-orange-500 mr-1.5 shrink-0" />
            <select
              value={currentLanguage}
              onChange={(e) => setLanguage(e.target.value)}
              className="bg-transparent text-xs sm:text-sm font-black text-slate-950 dark:text-white focus:outline-none cursor-pointer pr-1"
              title="Change Language"
              aria-label="Language selector"
            >
              <option value="en" className="bg-white dark:bg-slate-800 text-slate-950 dark:text-white">English</option>
              <option value="ta" className="bg-white dark:bg-slate-800 text-slate-950 dark:text-white">தமிழ் (Tamil)</option>
              <option value="hi" className="bg-white dark:bg-slate-800 text-slate-950 dark:text-white">हिंदी (Hindi)</option>
              <option value="te" className="bg-white dark:bg-slate-800 text-slate-950 dark:text-white">తెలుగు (Telugu)</option>
              <option value="kn" className="bg-white dark:bg-slate-800 text-slate-950 dark:text-white">ಕನ್ನಡ (Kannada)</option>
              <option value="ml" className="bg-white dark:bg-slate-800 text-slate-950 dark:text-white">മലയാളം (Malayalam)</option>
              <option value="bn" className="bg-white dark:bg-slate-800 text-slate-950 dark:text-white">বাংলা (Bengali)</option>
            </select>
          </div>

          {/* Sign In Button / User Profile in the top-right corner */}
          {user ? (
            <div className="relative">
              <button
                onClick={() => setIsUserMenuOpen(!isUserMenuOpen)}
                className="flex items-center gap-2 px-3 sm:px-4 py-1.5 rounded-xl border-2 border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-950 dark:text-white hover:border-orange-500 text-xs sm:text-sm font-black transition shadow-2xs cursor-pointer"
              >
                <div className="w-6 h-6 rounded-lg bg-orange-500 text-white flex items-center justify-center text-xs font-black shadow-xs">
                  {user.fullName.charAt(0).toUpperCase()}
                </div>
                <span className="hidden sm:inline max-w-[120px] truncate">{user.fullName}</span>
                <ChevronDown className="w-3.5 h-3.5 text-slate-400" />
              </button>

              {isUserMenuOpen && (
                <div className="absolute right-0 mt-2 w-60 bg-white dark:bg-slate-800 rounded-2xl shadow-xl border-2 border-slate-300 dark:border-slate-700 py-2 z-50">
                  <div className="px-4 py-2 border-b border-slate-200 dark:border-slate-700">
                    <p className="text-xs font-black text-slate-950 dark:text-white truncate">{user.fullName}</p>
                    <p className="text-[11px] text-slate-500 dark:text-slate-400 truncate">{user.email}</p>
                    {user.role === 'ROLE_ADMIN' && (
                      <span className="inline-block mt-1 text-[10px] font-black bg-rose-100 dark:bg-rose-950 text-rose-800 dark:text-rose-300 px-2 py-0.5 rounded-full">
                        Administrator
                      </span>
                    )}
                  </div>
                  {user.role === 'ROLE_ADMIN' && (
                    <button
                      onClick={() => {
                        setCurrentTab('admin');
                        setIsUserMenuOpen(false);
                      }}
                      className="w-full text-left px-4 py-2.5 text-xs font-extrabold text-orange-600 dark:text-orange-400 hover:bg-orange-50 dark:hover:bg-slate-700 flex items-center gap-2 cursor-pointer"
                    >
                      <LayoutDashboard className="w-4 h-4 text-orange-500" />
                      Admin Room (Dashboard)
                    </button>
                  )}
                  <button
                    onClick={() => {
                      onOpenSettings();
                      setIsUserMenuOpen(false);
                    }}
                    className="w-full text-left px-4 py-2 text-xs font-bold text-slate-800 dark:text-slate-200 hover:bg-slate-100 dark:hover:bg-slate-700 flex items-center gap-2 cursor-pointer"
                  >
                    <Settings className="w-4 h-4 text-slate-500" />
                    Settings & Preferences
                  </button>
                  <button
                    onClick={() => {
                      logout();
                      setIsUserMenuOpen(false);
                    }}
                    className="w-full text-left px-4 py-2 text-xs font-bold text-red-600 hover:bg-red-50 dark:hover:bg-red-950/40 flex items-center gap-2 cursor-pointer"
                  >
                    <LogOut className="w-4 h-4 text-red-500" />
                    {t.signOut}
                  </button>
                </div>
              )}
            </div>
          ) : (
            <button
              onClick={onOpenAuth}
              className="flex items-center gap-1.5 sm:gap-2 px-3.5 sm:px-4 py-2 rounded-xl bg-orange-500 hover:bg-orange-600 text-white font-black text-xs sm:text-sm shadow-md shadow-orange-500/25 transition cursor-pointer"
            >
              <LogIn className="w-4 h-4" />
              <span>{t.signIn}</span>
            </button>
          )}
        </div>
      </div>
    </nav>
  );
};
