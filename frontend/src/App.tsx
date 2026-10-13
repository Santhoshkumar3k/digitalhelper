import React, { useState } from 'react';
import { AuthProvider } from './context/AuthContext';
import { AccessibilityBar } from './components/AccessibilityBar';
import { Navbar } from './components/Navbar';
import { AuthModal } from './components/AuthModal';
import { SettingsModal } from './components/SettingsModal';
import { HomeDashboard } from './views/HomeDashboard';
import { ChatAssistantView } from './views/ChatAssistantView';
import { ScreenshotExplainerView } from './views/ScreenshotExplainerView';
import { TranslatorView } from './views/TranslatorView';
import { ScamCheckerView } from './views/ScamCheckerView';
import { FormGuideView } from './views/FormGuideView';
import { SavedAnswersView } from './views/SavedAnswersView';
import { AdminDashboardView } from './views/AdminDashboardView';

const MainApp: React.FC = () => {
  const [currentTab, setCurrentTab] = useState<string>('home');
  const [initialPrompt, setInitialPrompt] = useState<string | undefined>(undefined);
  const [isAuthOpen, setIsAuthOpen] = useState<boolean>(false);
  const [isSettingsOpen, setIsSettingsOpen] = useState<boolean>(false);

  const handleNavigate = (tab: string, prompt?: string) => {
    setCurrentTab(tab);
    if (prompt) {
      setInitialPrompt(prompt);
    }
  };

  return (
    <div className="min-h-screen flex flex-col bg-white dark:bg-[#0b0f19] text-slate-900 dark:text-slate-100 transition-colors">
      {/* Top Accessibility & Safety Alert Bar */}
      <AccessibilityBar />

      {/* Main Navbar */}
      <Navbar
        currentTab={currentTab}
        setCurrentTab={setCurrentTab}
        onOpenAuth={() => setIsAuthOpen(true)}
        onOpenSettings={() => setIsSettingsOpen(true)}
      />

      {/* Main Content Area */}
      <main className="flex-1 flex flex-col min-h-0">
        {currentTab === 'home' && (
          <HomeDashboard
            onNavigate={handleNavigate}
            onOpenSettings={() => setIsSettingsOpen(true)}
          />
        )}
        {currentTab === 'assistant' && (
          <ChatAssistantView
            initialPrompt={initialPrompt}
            onClearInitialPrompt={() => setInitialPrompt(undefined)}
            onOpenSettings={() => setIsSettingsOpen(true)}
          />
        )}
        {currentTab === 'screenshot' && <ScreenshotExplainerView />}
        {currentTab === 'translate' && <TranslatorView />}
        {currentTab === 'scam' && <ScamCheckerView />}
        {currentTab === 'forms' && <FormGuideView />}
        {currentTab === 'saved' && <SavedAnswersView />}
        {currentTab === 'admin' && <AdminDashboardView />}
      </main>

      {/* Modals */}
      <AuthModal isOpen={isAuthOpen} onClose={() => setIsAuthOpen(false)} />
      <SettingsModal isOpen={isSettingsOpen} onClose={() => setIsSettingsOpen(false)} />
    </div>
  );
};

export default function App() {
  return (
    <AuthProvider>
      <MainApp />
    </AuthProvider>
  );
}
