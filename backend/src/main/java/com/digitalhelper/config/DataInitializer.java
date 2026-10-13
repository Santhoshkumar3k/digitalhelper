package com.digitalhelper.config;

import com.digitalhelper.model.Conversation;
import com.digitalhelper.model.Message;
import com.digitalhelper.model.SavedAnswer;
import com.digitalhelper.model.User;
import com.digitalhelper.model.UserPreference;
import com.digitalhelper.repository.ConversationRepository;
import com.digitalhelper.repository.MessageRepository;
import com.digitalhelper.repository.SavedAnswerRepository;
import com.digitalhelper.repository.UserPreferenceRepository;
import com.digitalhelper.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final UserPreferenceRepository preferenceRepository;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final SavedAnswerRepository savedAnswerRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           UserPreferenceRepository preferenceRepository,
                           ConversationRepository conversationRepository,
                           MessageRepository messageRepository,
                           SavedAnswerRepository savedAnswerRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.preferenceRepository = preferenceRepository;
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.savedAnswerRepository = savedAnswerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            log.info("Initializing pre-seeded users and sample data for DigitalHelper...");

            // 1. Seed Admin User
            User admin = new User(
                    "admin@digitalhelper.com",
                    passwordEncoder.encode("Admin@12345"),
                    "System Administrator",
                    "ROLE_ADMIN"
            );
            admin = userRepository.save(admin);
            UserPreference adminPref = new UserPreference(admin);
            preferenceRepository.save(adminPref);

            // 2. Seed Standard User
            User demoUser = new User(
                    "user@digitalhelper.com",
                    passwordEncoder.encode("User@12345"),
                    "Santhosh Kumar",
                    "ROLE_USER"
            );
            demoUser = userRepository.save(demoUser);
            UserPreference userPref = new UserPreference(demoUser);
            userPref.setPreferredLanguage("en");
            preferenceRepository.save(userPref);

            // 3. Seed Sample Conversation for demo user
            Conversation conv = new Conversation(demoUser, "How to stop unknown WhatsApp spam calls?", "WhatsApp & Social");
            conv = conversationRepository.save(conv);

            Message msg1 = new Message(conv, "user", "I keep getting calls from unknown international numbers on WhatsApp. How do I stop them?");
            messageRepository.save(msg1);

            Message msg2 = new Message(conv, "assistant",
                    "### 🛡️ How to Silence Unknown Callers on WhatsApp\n\n"
                    + "WhatsApp now includes a built-in privacy protection setting designed specifically to stop spam calls:\n\n"
                    + "1. Open **WhatsApp** and tap the **three dots (⋮)** in the top right corner.\n"
                    + "2. Go to **Settings** → **Privacy**.\n"
                    + "3. Scroll down and tap **Calls**.\n"
                    + "4. Turn ON the toggle for **'Silence unknown callers'**.\n\n"
                    + "💡 **What this does**: Your phone will not ring or vibrate for numbers not in your contacts. You will still see the call quietly in your Calls log so you don't miss anything important.");
            messageRepository.save(msg2);

            // 4. Seed Saved Answers
            SavedAnswer saved1 = new SavedAnswer(
                    demoUser,
                    "Never Enter UPI PIN to Receive Money",
                    "Someone sent a QR code saying scan to receive cashback",
                    "**Critical Rule of Digital Payments**: You only enter your UPI PIN (Google Pay, PhonePe, Paytm) to SEND money or check balance. You NEVER need to enter a PIN to receive funds. If anyone sends a QR code or asks for a PIN to receive a lottery or refund, it is 100% a scam.",
                    "Banking & Security",
                    "UPI, PIN, Scam, Security"
            );
            saved1.setFavorite(true);
            savedAnswerRepository.save(saved1);

            SavedAnswer saved2 = new SavedAnswer(
                    demoUser,
                    "Free Up Phone Storage Safely",
                    "Phone memory full warning",
                    "1. Delete large WhatsApp forwarded videos in Settings > Storage and Data > Manage Storage.\n2. In Google Photos, tap your profile icon > Free up space to remove photos already backed up.\n3. Uninstall apps you haven't opened in 6 months.",
                    "Device Help",
                    "Storage, Android, WhatsApp"
            );
            savedAnswerRepository.save(saved2);

            log.info("DigitalHelper sample data successfully seeded!");
        }
    }
}
