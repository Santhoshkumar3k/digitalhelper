import React, { createContext, useContext, useState, useEffect } from 'react';
import { User, UserPreferences, AuthResponse } from '../types';
import { authApi, userApi } from '../services/api';
import { AppTranslations, getTranslations } from '../translations';

interface AuthContextType {
  user: User | null;
  token: string | null;
  preferences: UserPreferences | null;
  isLoading: boolean;
  login: (email: string, pass: string) => Promise<AuthResponse>;
  register: (fullName: string, email: string, pass: string) => Promise<AuthResponse>;
  logout: () => void;
  updatePreferences: (prefs: Partial<UserPreferences>) => Promise<void>;
  deleteAccount: () => Promise<void>;
  fontSize: 'normal' | 'large' | 'xlarge';
  setFontSize: (size: 'normal' | 'large' | 'xlarge') => void;
  highContrast: boolean;
  setHighContrast: (val: boolean) => void;
  darkMode: boolean;
  setDarkMode: (val: boolean) => void;
  currentLanguage: string;
  setLanguage: (lang: string) => void;
  t: AppTranslations;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [token, setToken] = useState<string | null>(localStorage.getItem('digitalhelper_token'));
  const [preferences, setPreferences] = useState<UserPreferences | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);

  // Language state (default to saved in localStorage, or 'en')
  const [currentLanguage, setCurrentLanguage] = useState<string>(
    () => localStorage.getItem('digitalhelper_lang') || 'en'
  );

  // Accessibility state - Default to BRIGHT light mode for elderly readability
  const [fontSize, setFontSize] = useState<'normal' | 'large' | 'xlarge'>('normal');
  const [highContrast, setHighContrast] = useState<boolean>(false);
  const [darkMode, setDarkModeState] = useState<boolean>(() => {
    return localStorage.getItem('digitalhelper_dark') === 'true';
  });

  const setDarkMode = (val: boolean) => {
    setDarkModeState(val);
    localStorage.setItem('digitalhelper_dark', val ? 'true' : 'false');
  };

  const t = getTranslations(currentLanguage);

  // Apply root accessibility classes to html/body
  useEffect(() => {
    const root = document.documentElement;
    // Dark mode: only apply if explicitly enabled
    if (darkMode) {
      root.classList.add('dark');
    } else {
      root.classList.remove('dark');
    }
    // High contrast
    if (highContrast) {
      root.classList.add('contrast-125');
      root.style.setProperty('--contrast-mode', 'high');
    } else {
      root.classList.remove('contrast-125');
      root.style.removeProperty('--contrast-mode');
    }
    // Font scale
    if (fontSize === 'large') {
      root.style.setProperty('--font-scale', '1.15');
    } else if (fontSize === 'xlarge') {
      root.style.setProperty('--font-scale', '1.3');
    } else {
      root.style.setProperty('--font-scale', '1');
    }
  }, [darkMode, highContrast, fontSize]);

  const setLanguage = (lang: string) => {
    setCurrentLanguage(lang);
    localStorage.setItem('digitalhelper_lang', lang);
    if (user && token) {
      userApi.updatePreferences({ preferredLanguage: lang }).catch(() => {});
    }
  };

  // Load user profile on startup if token exists
  useEffect(() => {
    async function loadUser() {
      if (!token) {
        setIsLoading(false);
        return;
      }
      try {
        const u = await userApi.getMe();
        setUser(u);
        const prefs = await userApi.getPreferences();
        setPreferences(prefs);
        if (prefs) {
          if (prefs.preferredLanguage) {
            setCurrentLanguage(prefs.preferredLanguage);
            localStorage.setItem('digitalhelper_lang', prefs.preferredLanguage);
          }
          if (prefs.largeFont) setFontSize('large');
          if (prefs.highContrast) setHighContrast(true);
          if (prefs.theme === 'dark') setDarkMode(true);
        }
      } catch (err) {
        console.warn('Session expired or backend unavailable', err);
        localStorage.removeItem('digitalhelper_token');
        setToken(null);
        setUser(null);
      } finally {
        setIsLoading(false);
      }
    }
    loadUser();
  }, [token]);

  const login = async (email: string, pass: string): Promise<AuthResponse> => {
    const res = await authApi.login(email, pass);
    localStorage.setItem('digitalhelper_token', res.token);
    setToken(res.token);
    setUser(res.user);
    try {
      const prefs = await userApi.getPreferences();
      setPreferences(prefs);
      if (prefs?.preferredLanguage) {
        setLanguage(prefs.preferredLanguage);
      }
    } catch {
      // ignore
    }
    return res;
  };

  const register = async (fullName: string, email: string, pass: string): Promise<AuthResponse> => {
    const res = await authApi.register(fullName, email, pass);
    localStorage.setItem('digitalhelper_token', res.token);
    setToken(res.token);
    setUser(res.user);
    return res;
  };

  const logout = () => {
    authApi.logout().catch(() => {});
    localStorage.removeItem('digitalhelper_token');
    setToken(null);
    setUser(null);
    setPreferences(null);
  };

  const updatePreferences = async (newPrefs: Partial<UserPreferences>) => {
    const updated = await userApi.updatePreferences(newPrefs);
    setPreferences(updated);
    if (newPrefs.preferredLanguage) {
      setLanguage(newPrefs.preferredLanguage);
    }
  };

  const deleteAccount = async () => {
    await userApi.deleteAccount();
    logout();
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        preferences,
        isLoading,
        login,
        register,
        logout,
        updatePreferences,
        deleteAccount,
        fontSize,
        setFontSize,
        highContrast,
        setHighContrast,
        darkMode,
        setDarkMode,
        currentLanguage,
        setLanguage,
        t,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within an AuthProvider');
  return ctx;
};
