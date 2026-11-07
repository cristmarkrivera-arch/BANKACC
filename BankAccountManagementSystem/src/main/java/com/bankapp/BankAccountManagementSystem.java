package com.bankapp;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.security.SecureRandom;
import java.sql.*;
import java.text.NumberFormat;
import java.util.*;
import java.util.List;

/**
 * Educational desktop banking demo.
 * Data is stored locally in bank_system.db using SQLite.
 */
public class BankAccountManagementSystem extends JFrame {
    private static final Color NAVY = new Color(19, 38, 66);
    private static final Color NAVY_LIGHT = new Color(31, 57, 91);
    private static final Color BLUE = new Color(43, 105, 190);
    private static final Color BG = new Color(243, 246, 251);
    private static final Color TEXT = new Color(35, 48, 66);
    private static final Color MUTED = new Color(112, 126, 145);
    private static final Color GREEN = new Color(31, 148, 103);
    private static final Color RED = new Color(194, 67, 76);
    private static final Font BODY = new Font("SansSerif", Font.PLAIN, 13);
    private static final Font BOLD = new Font("SansSerif", Font.BOLD, 13);
    private static final NumberFormat MONEY = NumberFormat.getCurrencyInstance(new Locale("en", "PH"));

    private final Database db = new Database();
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel root = new JPanel(cardLayout);

    private User loggedInUser;
    private JTable accountTable;
    private DefaultTableModel tableModel;
    private JLabel welcomeLabel;
    private JLabel countValue;
    private JLabel balanceValue;
    private JTextField searchField;

    public BankAccountManagementSystem() {
        super("Bank Account Management System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1050, 680));
        setSize(1180, 760);
        setLocationRelativeTo(null);
        setContentPane(root);

        try {
            db.initialize();
        } catch (SQLException ex) {
            showFatal("Could not initialize the database.\n" + ex.getMessage());
            return;
        }

        root.add(buildLoginPage(), "LOGIN");
        root.add(buildRegisterPage(), "REGISTER");
        root.add(buildDashboard(), "DASHBOARD");
        cardLayout.show(root, "LOGIN");
        setVisible(true);
    }

    // --------------------------- LOGIN PAGE ---------------------------
    private JPanel buildLoginPage() {
        JPanel page = new JPanel(new GridBagLayout());
        page.setBackground(BG);

        JPanel shell = new JPanel(new GridLayout(1, 2, 0, 0));
        shell.setPreferredSize(new Dimension(850, 510));
        shell.setBorder(BorderFactory.createLineBorder(new Color(224, 231, 240)));

        JPanel brand = new JPanel();
        brand.setBackground(NAVY);
        brand.setLayout(new BoxLayout(brand, BoxLayout.Y_AXIS));
        brand.setBorder(new EmptyBorder(55, 42, 45, 42));

        JLabel logo = new JLabel("◆  NORTHSTAR BANK");
        logo.setForeground(Color.WHITE);
        logo.setFont(new Font("SansSerif", Font.BOLD, 17));
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel headline = new JLabel("<html>Banking made<br>simple and secure.</html>");
        headline.setForeground(Color.WHITE);
        headline.setFont(new Font("SansSerif", Font.BOLD, 31));
        headline.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("<html><div style='width:260px'>Manage your accounts, review balances, and keep track of your transactions in one place.</div></html>");
        sub.setForeground(new Color(205, 218, 235));
        sub.setFont(BODY);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel emblem = new JLabel("◈");
        emblem.setForeground(new Color(115, 166, 232));
        emblem.setFont(new Font("SansSerif", Font.PLAIN, 95));
        emblem.setAlignmentX(Component.LEFT_ALIGNMENT);

        brand.add(logo);
        brand.add(Box.createVerticalStrut(65));
        brand.add(headline);
        brand.add(Box.createVerticalStrut(18));
        brand.add(sub);
        brand.add(Box.createVerticalGlue());
        brand.add(emblem);

        JPanel form = new JPanel();
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(55, 48, 45, 48));
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        JLabel title = heading("Welcome back");
        JLabel hint = subheading("Sign in to continue to your dashboard.");

        JTextField username = styledField();
        JPasswordField password = styledPassword();

        JButton login = primaryButton("Sign in");
        JButton goRegister = linkButton("Create a new account");

        form.add(title);
        form.add(Box.createVerticalStrut(8));
        form.add(hint);
        form.add(Box.createVerticalStrut(34));
        form.add(fieldLabel("USERNAME"));
        form.add(Box.createVerticalStrut(8));
        form.add(username);
        form.add(Box.createVerticalStrut(19));
        form.add(fieldLabel("PASSWORD"));
        form.add(Box.createVerticalStrut(8));
        form.add(password);
        form.add(Box.createVerticalStrut(27));
        form.add(login);
        form.add(Box.createVerticalStrut(18));
        form.add(centered(goRegister));
        form.add(Box.createVerticalGlue());

        login.addActionListener(e -> {
            String user = username.getText().trim();
            char[] pass = password.getPassword();
            if (user.isEmpty() || pass.length == 0) {
                warning("Enter your username and password.");
                return;
            }
            try {
                User found = db.authenticate(user, pass);
                Arrays.fill(pass, '\0');
                if (found == null) {
                    warning("Incorrect username or password.");
                } else {
                    loggedInUser = found;
                    password.setText("");
                    openDashboard();
                }
            } catch (SQLException ex) {
                error("Login failed: " + ex.getMessage());
            }
        });
        password.addActionListener(e -> login.doClick());
        goRegister.addActionListener(e -> cardLayout.show(root, "REGISTER"));

        shell.add(brand);
        shell.add(form);
        page.add(shell);
        return page;
    }

    // ------------------------- REGISTRATION PAGE -------------------------
    private JPanel buildRegisterPage() {
        JPanel page = new JPanel(new GridBagLayout());
        page.setBackground(BG);

        JPanel card = new JPanel();
        card.setBackground(Color.WHITE);
        card.setPreferredSize(new Dimension(490, 610));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(224, 231, 240)),
                new EmptyBorder(38, 48, 38, 48)));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel title = heading("Create your account");
        JLabel hint = subheading("Register once to access your personal dashboard.");

        JTextField fullName = styledField();
        JTextField username = styledField();
        JPasswordField password = styledPassword();
        JPasswordField confirm = styledPassword();

        JButton create = primaryButton("Create account");
        JButton back = linkButton("Back to sign in");

        card.add(title);
        card.add(Box.createVerticalStrut(7));
        card.add(hint);
        card.add(Box.createVerticalStrut(25));
        addLabeledField(card, "FULL NAME", fullName);
        addLabeledField(card, "USERNAME", username);
        addLabeledField(card, "PASSWORD (AT LEAST 8 CHARACTERS)", password);
        addLabeledField(card, "CONFIRM PASSWORD", confirm);
        card.add(Box.createVerticalStrut(8));
        card.add(create);
        card.add(Box.createVerticalStrut(14));
        card.add(centered(back));

        create.addActionListener(e -> {
            String name = fullName.getText().trim();
            String user = username.getText().trim();
            char[] p1 = password.getPassword();
            char[] p2 = confirm.getPassword();

            if (name.isEmpty() || user.isEmpty() || p1.length == 0 || p2.length == 0) {
                warning("Please complete all registration fields.");
                return;
            }
            if (user.length() < 4) {
                warning("Username must contain at least 4 characters.");
                return;
            }
            if (p1.length < 8) {
                warning("Password must contain at least 8 characters.");
                return;
            }
            if (!Arrays.equals(p1, p2)) {
                warning("Passwords do not match.");
                return;
            }

            try {
                long userId = db.register(name, user, p1);
                Arrays.fill(p1, '\0');
                Arrays.fill(p2, '\0');
                loggedInUser = new User(userId, name, user);
                openDashboard();
                info("Your account was created successfully. Welcome, " + name + "!");
            } catch (SQLIntegrityConstraintViolationException ex) {
                error("That username is already taken.");
            } catch (SQLException ex) {
                if (ex.getMessage() != null && ex.getMessage().toLowerCase().contains("unique")) {
                    error("That username is already taken.");
                } else {
                    error("Could not create account: " + ex.getMessage());
                }
            }
        });
        back.addActionListener(e -> cardLayout.show(root, "LOGIN"));

        page.add(card);
        return page;
    }

    // ----------------------------- DASHBOARD -----------------------------
    private JPanel buildDashboard() {
        JPanel page = new JPanel(new BorderLayout());
        page.setBackground(BG);

        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBackground(NAVY);
        sidebar.setBorder(new EmptyBorder(25, 17, 22, 17));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));

        JLabel brand = new JLabel("◆  NORTHSTAR");
        brand.setForeground(Color.WHITE);
        brand.setFont(new Font("SansSerif", Font.BOLD, 18));
        brand.setBorder(new EmptyBorder(5, 8, 32, 5));
        sidebar.add(brand);

        JLabel menuLabel = new JLabel("  WORKSPACE");
        menuLabel.setForeground(new Color(150, 171, 199));
        menuLabel.setFont(new Font("SansSerif", Font.BOLD, 10));
        sidebar.add(menuLabel);
        sidebar.add(Box.createVerticalStrut(12));

        addSideButton(sidebar, "▦   Dashboard", this::refreshDashboard);
        addSideButton(sidebar, "＋   Open bank account", this::createBankAccount);
        addSideButton(sidebar, "⇧   Deposit money", () -> transaction("DEPOSIT"));
        addSideButton(sidebar, "⇩   Withdraw money", () -> transaction("WITHDRAW"));
        addSideButton(sidebar, "◉   Check balance", this::checkBalance);
        addSideButton(sidebar, "⌕   Search account", this::focusSearch);
        addSideButton(sidebar, "✎   Update account", this::updateAccount);
        addSideButton(sidebar, "×   Delete account", this::deleteAccount);

        sidebar.add(Box.createVerticalGlue());
        JButton logout = new JButton("↪   Log out");
        styleSideButton(logout, true);
        logout.addActionListener(e -> logout());
        sidebar.add(logout);

        JPanel content = new JPanel(new BorderLayout(22, 18));
        content.setBackground(BG);
        content.setBorder(new EmptyBorder(25, 28, 22, 28));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        JLabel pageTitle = new JLabel("Dashboard");
        pageTitle.setFont(new Font("SansSerif", Font.BOLD, 25));
        pageTitle.setForeground(TEXT);
        welcomeLabel = new JLabel("Welcome back");
        welcomeLabel.setFont(BODY);
        welcomeLabel.setForeground(MUTED);
        titles.add(pageTitle);
        titles.add(Box.createVerticalStrut(5));
        titles.add(welcomeLabel);
        top.add(titles, BorderLayout.WEST);

        JLabel userChip = new JLabel("  ●  ACCOUNT HOLDER  ");
        userChip.setOpaque(true);
        userChip.setBackground(Color.WHITE);
        userChip.setForeground(BLUE);
        userChip.setFont(new Font("SansSerif", Font.BOLD, 10));
        userChip.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 232, 241)),
                new EmptyBorder(10, 8, 10, 8)));
        top.add(userChip, BorderLayout.EAST);

        JPanel stats = new JPanel(new GridLayout(1, 2, 16, 0));
        stats.setOpaque(false);
        countValue = new JLabel("0");
        balanceValue = new JLabel(MONEY.format(0));
        stats.add(statCard("TOTAL ACCOUNTS", countValue, "Your active bank accounts", "▦"));
        stats.add(statCard("TOTAL BALANCE", balanceValue, "Combined account balance", "₱"));

        JPanel tableCard = new JPanel(new BorderLayout(0, 14));
        tableCard.setBackground(Color.WHITE);
        tableCard.setBorder(new EmptyBorder(18, 18, 18, 18));

        JPanel tableTop = new JPanel(new BorderLayout(12, 0));
        tableTop.setOpaque(false);
        JLabel tableTitle = new JLabel("Your bank accounts");
        tableTitle.setFont(new Font("SansSerif", Font.BOLD, 17));
        tableTitle.setForeground(TEXT);
        searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(240, 36));
        searchField.setFont(BODY);
        searchField.setToolTipText("Search account number, holder, or type");
        searchField.putClientProperty("JTextField.placeholderText", "Search accounts...");
        searchField.addActionListener(e -> refreshTable());
        JButton search = new JButton("Search");
        styleSmallButton(search);
        search.addActionListener(e -> refreshTable());
        JPanel searchPanel = new JPanel(new BorderLayout(7, 0));
        searchPanel.setOpaque(false);
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(search, BorderLayout.EAST);
        tableTop.add(tableTitle, BorderLayout.WEST);
        tableTop.add(searchPanel, BorderLayout.EAST);

        tableModel = new DefaultTableModel(
                new String[]{"ACCOUNT NUMBER", "ACCOUNT HOLDER", "ACCOUNT TYPE", "CURRENT BALANCE"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        accountTable = new JTable(tableModel);
        accountTable.setRowHeight(39);
        accountTable.setFont(BODY);
        accountTable.setForeground(TEXT);
        accountTable.setSelectionBackground(new Color(225, 237, 252));
        accountTable.setSelectionForeground(TEXT);
        accountTable.setGridColor(new Color(238, 242, 247));
        accountTable.setShowVerticalLines(false);
        accountTable.setIntercellSpacing(new Dimension(0, 1));
        accountTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 10));
        accountTable.getTableHeader().setForeground(MUTED);
        accountTable.getTableHeader().setBackground(new Color(248, 250, 253));
        accountTable.getTableHeader().setPreferredSize(new Dimension(0, 38));
        JScrollPane scroll = new JScrollPane(accountTable);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(235, 239, 245)));

        JLabel note = new JLabel("Select an account row to use it for transactions and account actions.");
        note.setForeground(MUTED);
        note.setFont(new Font("SansSerif", Font.PLAIN, 11));

        tableCard.add(tableTop, BorderLayout.NORTH);
        tableCard.add(scroll, BorderLayout.CENTER);
        tableCard.add(note, BorderLayout.SOUTH);

        content.add(top, BorderLayout.NORTH);
        content.add(stats, BorderLayout.CENTER);
        content.add(tableCard, BorderLayout.SOUTH);

        // Use a vertical wrapper so the table gets most of the available height.
        JPanel center = new JPanel(new BorderLayout(0, 18));
        center.setOpaque(false);
        center.add(stats, BorderLayout.NORTH);
        center.add(tableCard, BorderLayout.CENTER);
        content.remove(stats);
        content.add(center, BorderLayout.CENTER);

        page.add(sidebar, BorderLayout.WEST);
        page.add(content, BorderLayout.CENTER);
        return page;
    }

    private JPanel statCard(String title, JLabel value, String subtitle, String symbol) {
        JPanel card = new JPanel(new BorderLayout(10, 7));
        card.setBackground(Color.WHITE);
        card.setBorder(new EmptyBorder(17, 20, 17, 20));
        JLabel label = new JLabel(title);
        label.setFont(new Font("SansSerif", Font.BOLD, 10));
        label.setForeground(MUTED);
        value.setFont(new Font("SansSerif", Font.BOLD, 25));
        value.setForeground(TEXT);
        JLabel sub = new JLabel(subtitle);
        sub.setFont(new Font("SansSerif", Font.PLAIN, 11));
        sub.setForeground(MUTED);
        JLabel icon = new JLabel(symbol);
        icon.setFont(new Font("SansSerif", Font.BOLD, 24));
        icon.setForeground(BLUE);
        JPanel words = new JPanel();
        words.setOpaque(false);
        words.setLayout(new BoxLayout(words, BoxLayout.Y_AXIS));
        words.add(label);
        words.add(Box.createVerticalStrut(9));
        words.add(value);
        words.add(Box.createVerticalStrut(5));
        words.add(sub);
        card.add(words, BorderLayout.CENTER);
        card.add(icon, BorderLayout.EAST);
        return card;
    }

    // --------------------------- AUTH / NAVIGATION ---------------------------
    private void openDashboard() {
        welcomeLabel.setText("Welcome back, " + loggedInUser.fullName() + ".");
        cardLayout.show(root, "DASHBOARD");
        refreshDashboard();
    }

    private void logout() {
        int answer = JOptionPane.showConfirmDialog(this, "Are you sure you want to log out?",
                "Confirm logout", JOptionPane.YES_NO_OPTION);
        if (answer == JOptionPane.YES_OPTION) {
            loggedInUser = null;
            cardLayout.show(root, "LOGIN");
        }
    }

    // ----------------------------- BANK ACTIONS -----------------------------
    private void createBankAccount() {
        JTextField holder = new JTextField(loggedInUser.fullName(), 18);
        JComboBox<String> type = new JComboBox<>(new String[]{"Savings", "Checking"});
        JTextField initial = new JTextField("500.00", 18);

        JPanel panel = formPanel(
                new String[]{"Account holder", "Account type", "Initial deposit (PHP)"},
                new JComponent[]{holder, type, initial});
        int result = JOptionPane.showConfirmDialog(this, panel, "Open a bank account",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        try {
            String name = holder.getText().trim();
            String accountType = Objects.toString(type.getSelectedItem());
            double amount = parseMoney(initial.getText(), true);
            if (name.isEmpty()) throw new IllegalArgumentException("Account holder name is required.");
            if (accountType.equals("Savings") && amount < 500)
                throw new IllegalArgumentException("Savings accounts require a minimum opening deposit of ₱500.");
            String number = db.createBankAccount(loggedInUser.id(), name, accountType, amount);
            refreshDashboard();
            info("Bank account created successfully!\n\nAccount number: " + number
                    + "\nAccount holder: " + name + "\nAccount type: " + accountType
                    + "\nInitial balance: " + MONEY.format(amount));
        } catch (Exception ex) {
            error(ex.getMessage());
        }
    }

    private void transaction(String type) {
        AccountRow selected = selectedAccount();
        if (selected == null) {
            warning("Select an account from the table first.");
            return;
        }

        JTextField amountField = new JTextField(15);
        String action = type.equals("DEPOSIT") ? "Deposit" : "Withdraw";
        JPanel panel = formPanel(new String[]{"Account number", "Amount (PHP)"},
                new JComponent[]{readOnlyField(selected.accountNumber()), amountField});
        int result = JOptionPane.showConfirmDialog(this, panel, action + " money",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        try {
            double amount = parseMoney(amountField.getText(), false);
            db.applyTransaction(loggedInUser.id(), selected.accountNumber(), amount, type);
            refreshDashboard();
            info(action + " successful.\nNew balance: " +
                    MONEY.format(db.getBalance(loggedInUser.id(), selected.accountNumber())));
        } catch (Exception ex) {
            error(ex.getMessage());
        }
    }

    private void checkBalance() {
        AccountRow row = selectedAccount();
        if (row == null) {
            warning("Select an account from the table first.");
            return;
        }
        try {
            double balance = db.getBalance(loggedInUser.id(), row.accountNumber());
            info("ACCOUNT BALANCE\n\nAccount number: " + row.accountNumber()
                    + "\nAccount holder: " + row.holder()
                    + "\nAccount type: " + row.type()
                    + "\nAvailable balance: " + MONEY.format(balance));
        } catch (SQLException ex) {
            error(ex.getMessage());
        }
    }

    private void updateAccount() {
        AccountRow row = selectedAccount();
        if (row == null) {
            warning("Select an account from the table first.");
            return;
        }
        JTextField name = new JTextField(row.holder(), 20);
        JPanel panel = formPanel(new String[]{"New account holder name"}, new JComponent[]{name});
        int result = JOptionPane.showConfirmDialog(this, panel, "Update account",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;
        try {
            if (name.getText().trim().isEmpty())
                throw new IllegalArgumentException("Account holder name cannot be empty.");
            db.updateAccount(loggedInUser.id(), row.accountNumber(), name.getText().trim());
            refreshDashboard();
            info("Account details updated.");
        } catch (Exception ex) {
            error(ex.getMessage());
        }
    }

    private void deleteAccount() {
        AccountRow row = selectedAccount();
        if (row == null) {
            warning("Select an account from the table first.");
            return;
        }
        int answer = JOptionPane.showConfirmDialog(this,
                "Permanently delete account " + row.accountNumber() + "?\nThis cannot be undone.",
                "Delete account", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (answer != JOptionPane.YES_OPTION) return;
        try {
            db.deleteAccount(loggedInUser.id(), row.accountNumber());
            refreshDashboard();
            info("Account deleted.");
        } catch (SQLException ex) {
            error(ex.getMessage());
        }
    }

    private void focusSearch() {
        searchField.requestFocusInWindow();
        searchField.selectAll();
    }

    private AccountRow selectedAccount() {
        int viewRow = accountTable.getSelectedRow();
        if (viewRow < 0) return null;
        int row = accountTable.convertRowIndexToModel(viewRow);
        return new AccountRow(
                tableModel.getValueAt(row, 0).toString(),
                tableModel.getValueAt(row, 1).toString(),
                tableModel.getValueAt(row, 2).toString(),
                tableModel.getValueAt(row, 3).toString());
    }

    private void refreshDashboard() {
        refreshTable();
        try {
            List<AccountRow> rows = db.getAccounts(loggedInUser.id());
            double total = db.getTotalBalance(loggedInUser.id());
            countValue.setText(String.valueOf(rows.size()));
            balanceValue.setText(MONEY.format(total));
            welcomeLabel.setText("Welcome back, " + loggedInUser.fullName() + ".");
        } catch (SQLException ex) {
            error("Could not load dashboard: " + ex.getMessage());
        }
    }

    private void refreshTable() {
        if (loggedInUser == null || tableModel == null) return;
        tableModel.setRowCount(0);
        try {
            String filter = searchField == null ? "" : searchField.getText().trim().toLowerCase();
            for (AccountRow row : db.getAccounts(loggedInUser.id())) {
                String combined = (row.accountNumber() + " " + row.holder() + " " + row.type()).toLowerCase();
                if (filter.isEmpty() || combined.contains(filter)) {
                    tableModel.addRow(new Object[]{
                            row.accountNumber(), row.holder(), row.type(), row.balanceText()
                    });
                }
            }
        } catch (SQLException ex) {
            error("Could not load accounts: " + ex.getMessage());
        }
    }

    // ------------------------------- UI HELPERS -------------------------------
    private void addSideButton(JPanel panel, String text, Runnable action) {
        JButton button = new JButton(text);
        styleSideButton(button, false);
        button.addActionListener(e -> action.run());
        panel.add(button);
        panel.add(Box.createVerticalStrut(7));
    }

    private void styleSideButton(JButton button, boolean logout) {
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 43));
        button.setPreferredSize(new Dimension(185, 43));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setFont(BOLD);
        button.setForeground(logout ? new Color(255, 211, 211) : new Color(225, 234, 246));
        button.setBackground(logout ? NAVY_LIGHT : NAVY);
        button.setBorder(new EmptyBorder(9, 11, 9, 8));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void styleSmallButton(JButton button) {
        button.setBackground(BLUE);
        button.setForeground(Color.WHITE);
        button.setFont(BOLD);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(9, 15, 9, 15));
    }

    private JButton primaryButton(String text) {
        JButton button = new JButton(text);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 43));
        button.setPreferredSize(new Dimension(300, 43));
        button.setBackground(BLUE);
        button.setForeground(Color.WHITE);
        button.setFont(BOLD);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(10, 15, 10, 15));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private JButton linkButton(String text) {
        JButton button = new JButton(text);
        button.setForeground(BLUE);
        button.setFont(BOLD);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private JLabel heading(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 25));
        label.setForeground(TEXT);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JLabel subheading(String text) {
        JLabel label = new JLabel(text);
        label.setFont(BODY);
        label.setForeground(MUTED);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JLabel fieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 10));
        label.setForeground(MUTED);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JTextField styledField() {
        JTextField field = new JTextField();
        field.setFont(BODY);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        field.setPreferredSize(new Dimension(300, 40));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 228, 238)),
                new EmptyBorder(8, 11, 8, 11)));
        return field;
    }

    private JPasswordField styledPassword() {
        JPasswordField field = new JPasswordField();
        field.setFont(BODY);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        field.setPreferredSize(new Dimension(300, 40));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 228, 238)),
                new EmptyBorder(8, 11, 8, 11)));
        return field;
    }

    private JPanel centered(JComponent component) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        panel.setOpaque(false);
        panel.add(component);
        return panel;
    }

    private void addLabeledField(JPanel panel, String label, JComponent field) {
        panel.add(fieldLabel(label));
        panel.add(Box.createVerticalStrut(6));
        panel.add(field);
        panel.add(Box.createVerticalStrut(14));
    }

    private JPanel formPanel(String[] labels, JComponent[] fields) {
        JPanel panel = new JPanel(new GridLayout(labels.length, 2, 12, 12));
        panel.setBorder(new EmptyBorder(12, 8, 8, 8));
        for (int i = 0; i < labels.length; i++) {
            JLabel label = new JLabel(labels[i]);
            label.setFont(BODY);
            panel.add(label);
            panel.add(fields[i]);
        }
        return panel;
    }

    private JTextField readOnlyField(String text) {
        JTextField field = new JTextField(text);
        field.setEditable(false);
        return field;
    }

    private double parseMoney(String value, boolean allowZero) {
        try {
            double amount = Double.parseDouble(value.trim().replace(",", ""));
            if (!Double.isFinite(amount) || amount < 0 || (!allowZero && amount == 0))
                throw new IllegalArgumentException("Enter a valid positive amount.");
            return amount;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Enter a valid numeric amount.");
        }
    }

    private void info(String message) {
        JOptionPane.showMessageDialog(this, message, "Northstar Bank", JOptionPane.INFORMATION_MESSAGE);
    }
    private void warning(String message) {
        JOptionPane.showMessageDialog(this, message, "Please check", JOptionPane.WARNING_MESSAGE);
    }
    private void error(String message) {
        JOptionPane.showMessageDialog(this, message == null ? "An unexpected error occurred." : message,
                "Error", JOptionPane.ERROR_MESSAGE);
    }
    private void showFatal(String message) {
        JOptionPane.showMessageDialog(null, message, "Startup error", JOptionPane.ERROR_MESSAGE);
        dispose();
    }

    // --------------------------- DATA / OOP CLASSES ---------------------------
    private record User(long id, String fullName, String username) {}
    private record AccountRow(String accountNumber, String holder, String type, String balanceText) {}

    private static class Database {
        private static final String URL = "jdbc:sqlite:bank_system.db";

        private Connection connect() throws SQLException {
            Connection connection = DriverManager.getConnection(URL);
            try (Statement statement = connection.createStatement()) {
                statement.execute("PRAGMA foreign_keys = ON");
                statement.execute("PRAGMA busy_timeout = 5000");
            }
            return connection;
        }

        void initialize() throws SQLException {
            try (Connection c = connect(); Statement s = c.createStatement()) {
                s.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS users (
                        user_id INTEGER PRIMARY KEY AUTOINCREMENT,
                        full_name TEXT NOT NULL,
                        username TEXT NOT NULL COLLATE NOCASE UNIQUE,
                        password_hash TEXT NOT NULL,
                        password_salt TEXT NOT NULL,
                        created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
                    )
                    """);
                s.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS bank_accounts (
                        account_id INTEGER PRIMARY KEY AUTOINCREMENT,
                        account_number TEXT NOT NULL UNIQUE,
                        owner_user_id INTEGER NOT NULL,
                        account_holder TEXT NOT NULL,
                        account_type TEXT NOT NULL CHECK(account_type IN ('Savings','Checking')),
                        balance REAL NOT NULL CHECK(balance >= 0),
                        created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY(owner_user_id) REFERENCES users(user_id) ON DELETE CASCADE
                    )
                    """);
                s.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS transactions (
                        transaction_id INTEGER PRIMARY KEY AUTOINCREMENT,
                        account_number TEXT NOT NULL,
                        transaction_type TEXT NOT NULL,
                        amount REAL NOT NULL CHECK(amount > 0),
                        transaction_time TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY(account_number) REFERENCES bank_accounts(account_number) ON DELETE CASCADE
                    )
                    """);
            }
        }

        long register(String fullName, String username, char[] password) throws SQLException {
            byte[] salt = new byte[16];
            new SecureRandom().nextBytes(salt);
            String saltText = Base64.getEncoder().encodeToString(salt);
            String hash = hashPassword(password, salt);
            String sql = "INSERT INTO users(full_name, username, password_hash, password_salt) VALUES(?,?,?,?)";
            try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, fullName);
                ps.setString(2, username);
                ps.setString(3, hash);
                ps.setString(4, saltText);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) return rs.getLong(1);
                }
            }
            throw new SQLException("Could not retrieve the new user ID.");
        }

        User authenticate(String username, char[] password) throws SQLException {
            String sql = "SELECT user_id, full_name, username, password_hash, password_salt FROM users WHERE username = ?";
            try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) return null;
                    byte[] salt = Base64.getDecoder().decode(rs.getString("password_salt"));
                    String expected = rs.getString("password_hash");
                    String actual = hashPassword(password, salt);
                    if (!constantTimeEquals(expected, actual)) return null;
                    return new User(rs.getLong("user_id"), rs.getString("full_name"), rs.getString("username"));
                }
            }
        }

        String createBankAccount(long ownerId, String holder, String type, double initial) throws SQLException {
            String number;
            do {
                number = "BA" + (100000 + new SecureRandom().nextInt(900000));
            } while (accountNumberExists(number));

            String sql = "INSERT INTO bank_accounts(account_number, owner_user_id, account_holder, account_type, balance) VALUES(?,?,?,?,?)";
            try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, number);
                ps.setLong(2, ownerId);
                ps.setString(3, holder);
                ps.setString(4, type);
                ps.setDouble(5, initial);
                ps.executeUpdate();
            }
            if (initial > 0) recordTransaction(number, "OPENING DEPOSIT", initial);
            return number;
        }

        private boolean accountNumberExists(String number) throws SQLException {
            try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(
                    "SELECT 1 FROM bank_accounts WHERE account_number=?")) {
                ps.setString(1, number);
                try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
            }
        }

        List<AccountRow> getAccounts(long ownerId) throws SQLException {
            List<AccountRow> list = new ArrayList<>();
            String sql = "SELECT account_number, account_holder, account_type, balance FROM bank_accounts WHERE owner_user_id=? ORDER BY account_id DESC";
            try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setLong(1, ownerId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) list.add(new AccountRow(
                            rs.getString("account_number"),
                            rs.getString("account_holder"),
                            rs.getString("account_type"),
                            MONEY.format(rs.getDouble("balance"))));
                }
            }
            return list;
        }

        double getTotalBalance(long ownerId) throws SQLException {
            try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(
                    "SELECT COALESCE(SUM(balance),0) FROM bank_accounts WHERE owner_user_id=?")) {
                ps.setLong(1, ownerId);
                try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getDouble(1) : 0; }
            }
        }

        double getBalance(long ownerId, String number) throws SQLException {
            try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(
                    "SELECT balance FROM bank_accounts WHERE owner_user_id=? AND account_number=?")) {
                ps.setLong(1, ownerId);
                ps.setString(2, number);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) throw new SQLException("Account not found.");
                    return rs.getDouble(1);
                }
            }
        }

        void applyTransaction(long ownerId, String number, double amount, String type) throws SQLException {
            try (Connection c = connect()) {
                c.setAutoCommit(false);
                try {
                    String select = "SELECT balance, account_type FROM bank_accounts WHERE owner_user_id=? AND account_number=?";
                    double balance;
                    String accountType;
                    try (PreparedStatement ps = c.prepareStatement(select)) {
                        ps.setLong(1, ownerId);
                        ps.setString(2, number);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (!rs.next()) throw new SQLException("Account not found.");
                            balance = rs.getDouble("balance");
                            accountType = rs.getString("account_type");
                        }
                    }

                    double newBalance;
                    if ("DEPOSIT".equals(type)) {
                        newBalance = balance + amount;
                        if (!Double.isFinite(newBalance)) throw new SQLException("Amount is too large.");
                    } else {
                        newBalance = balance - amount;
                        if (newBalance < 0) throw new SQLException("Insufficient balance.");
                        if ("Savings".equals(accountType) && newBalance < 500)
                            throw new SQLException("Savings account must maintain a minimum balance of ₱500.");
                    }

                    try (PreparedStatement ps = c.prepareStatement(
                            "UPDATE bank_accounts SET balance=? WHERE owner_user_id=? AND account_number=?")) {
                        ps.setDouble(1, newBalance);
                        ps.setLong(2, ownerId);
                        ps.setString(3, number);
                        ps.executeUpdate();
                    }
                    try (PreparedStatement ps = c.prepareStatement(
                            "INSERT INTO transactions(account_number, transaction_type, amount) VALUES(?,?,?)")) {
                        ps.setString(1, number);
                        ps.setString(2, type);
                        ps.setDouble(3, amount);
                        ps.executeUpdate();
                    }
                    c.commit();
                } catch (Exception ex) {
                    c.rollback();
                    if (ex instanceof SQLException sql) throw sql;
                    throw new SQLException(ex.getMessage(), ex);
                } finally {
                    c.setAutoCommit(true);
                }
            }
        }

        void updateAccount(long ownerId, String number, String newName) throws SQLException {
            try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(
                    "UPDATE bank_accounts SET account_holder=? WHERE owner_user_id=? AND account_number=?")) {
                ps.setString(1, newName);
                ps.setLong(2, ownerId);
                ps.setString(3, number);
                if (ps.executeUpdate() == 0) throw new SQLException("Account not found.");
            }
        }

        void deleteAccount(long ownerId, String number) throws SQLException {
            try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(
                    "DELETE FROM bank_accounts WHERE owner_user_id=? AND account_number=?")) {
                ps.setLong(1, ownerId);
                ps.setString(2, number);
                if (ps.executeUpdate() == 0) throw new SQLException("Account not found.");
            }
        }

        private void recordTransaction(String number, String type, double amount) throws SQLException {
            try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO transactions(account_number, transaction_type, amount) VALUES(?,?,?)")) {
                ps.setString(1, number);
                ps.setString(2, type);
                ps.setDouble(3, amount);
                ps.executeUpdate();
            }
        }

        private static String hashPassword(char[] password, byte[] salt) {
            PBEKeySpec spec = new PBEKeySpec(password, salt, 120_000, 256);
            try {
                byte[] encoded = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                        .generateSecret(spec).getEncoded();
                return Base64.getEncoder().encodeToString(encoded);
            } catch (Exception ex) {
                throw new IllegalStateException("Password hashing is unavailable.", ex);
            } finally {
                spec.clearPassword();
            }
        }

        private static boolean constantTimeEquals(String a, String b) {
            return java.security.MessageDigest.isEqual(
                    a.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                    b.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            new BankAccountManagementSystem();
        });
    }
}
