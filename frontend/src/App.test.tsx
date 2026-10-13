import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import App from './App';

describe('DigitalHelper Frontend Application', () => {
  it('renders application branding and accessibility header', () => {
    render(<App />);

    // Brand and tagline
    expect(screen.getAllByText(/Digital/i).length).toBeGreaterThan(0);
    expect(screen.getAllByText(/Technology Made Simple for Everyone/i).length).toBeGreaterThan(0);

    // Accessibility bar safety notice
    expect(screen.getByText(/Safety Notice/i)).toBeDefined();

    // Quick Action Service Cards on Home Dashboard
    expect(screen.getByText(/AI Everyday Assistant/i)).toBeDefined();
    expect(screen.getByText(/Screenshot & Error Explainer/i)).toBeDefined();
    expect(screen.getByText(/Scam & Fake Message Checker/i)).toBeDefined();
  });
});
