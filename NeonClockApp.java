import java.awt.*;
import java.awt.event.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.sound.sampled.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class NeonClockApp extends JFrame {

    // UI accent colors
    private static final Color BG = new Color(10, 10, 20);
    private static final Color PANEL = new Color(15, 15, 28, 220);
    private static final Color NEON = new Color(0, 255, 180);    
    private static final Color NEON_ALT = new Color(255, 0, 150);
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("hh:mm:ss a");
    private static final DateTimeFormatter HOUR_MIN_FMT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy");

    // Core panels
    private ClockPanel clockPanel;
    private StopwatchPanel stopwatchPanel;
    private TimerPanel timerPanel;
    private AlarmPanel alarmPanel;

    public NeonClockApp() {
        super("Neon Digital Clock — Advanced");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(980, 640);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        initUI();
    }

    private void initUI() {
        // Top header with day/date
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(BG);
        top.setBorder(new EmptyBorder(12, 18, 12, 18));
        JLabel title = new JLabel("Neon Digital Clock");
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setForeground(NEON);
        JLabel subtitle = new JLabel("Animated • Stopwatch • Timer • Multi-Alarm");
        subtitle.setForeground(new Color(170, 200, 220));
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        JPanel left = new JPanel(new BorderLayout());
        left.setOpaque(false);
        left.add(title, BorderLayout.NORTH);
        left.add(subtitle, BorderLayout.SOUTH);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);
        JLabel dateLabel = new JLabel();
        dateLabel.setForeground(new Color(200, 220, 240));
        dateLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        // Update date label each second
            javax.swing.Timer dateTimer = new javax.swing.Timer(
             1000,
          e -> dateLabel.setText(LocalDate.now().format(DATE_FMT))
        );

        dateTimer.start();

        right.add(dateLabel);

        top.add(left, BorderLayout.WEST);
        top.add(right, BorderLayout.EAST);

        add(top, BorderLayout.NORTH);

        // Tabbed content
        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(BG);
        tabs.setForeground(new Color(200, 220, 240));
        tabs.setFont(new Font("SansSerif", Font.PLAIN, 14));

        clockPanel = new ClockPanel();
        stopwatchPanel = new StopwatchPanel();
        timerPanel = new TimerPanel();
        alarmPanel = new AlarmPanel();

        tabs.addTab("Clock", clockPanel);
        tabs.addTab("Stopwatch", stopwatchPanel);
        tabs.addTab("Timer", timerPanel);
        tabs.addTab("Alarms", alarmPanel);

        add(tabs, BorderLayout.CENTER);

        // Footer
        JLabel footer = new JLabel("Built with ♥ • Neon UI", SwingConstants.CENTER);
        footer.setOpaque(true);
        footer.setBackground(new Color(8, 10, 12));
        footer.setForeground(new Color(160, 200, 220));
        footer.setBorder(new EmptyBorder(8, 0, 8, 0));
        add(footer, BorderLayout.SOUTH);
    }

    // ------------------ Clock Panel ------------------
    private class ClockPanel extends JPanel {
        private JLabel timeLabel;
        private JLabel dayLabel;
        private javax.swing.Timer blinkTimer;
        private boolean colonOn = true;
        private float glowPhase = 0f;

        ClockPanel() {
            setOpaque(true);
            setBackground(BG);
            setLayout(new GridBagLayout());
            setBorder(new EmptyBorder(24, 24, 24, 24));

            JPanel glass = createGlassPanel(new BorderLayout());
            glass.setPreferredSize(new Dimension(760, 360));

            timeLabel = new JLabel("", SwingConstants.CENTER);
            timeLabel.setFont(new Font("Digital-7 Mono", Font.BOLD, 72));
            timeLabel.setForeground(NEON);
            timeLabel.setOpaque(false);

            dayLabel = new JLabel("", SwingConstants.CENTER);
            dayLabel.setFont(new Font("SansSerif", Font.PLAIN, 20));
            dayLabel.setForeground(new Color(190, 220, 240));

            glass.add(timeLabel, BorderLayout.CENTER);
            glass.add(dayLabel, BorderLayout.SOUTH);

            add(glass);

            // timers: update every 200ms for animation and blinking
            javax.swing.Timer t = new javax.swing.Timer(200, e -> updateTime());
            t.start();

            // glow animation
            blinkTimer = new javax.swing.Timer(60, e -> {
                glowPhase += 0.06f;
                if (glowPhase > (float)Math.PI * 2) glowPhase = 0f;
                float factor = 0.6f + 0.4f*(float)(0.5*(1+Math.sin(glowPhase)));
                Color glow = blend(NEON, Color.WHITE, factor*0.12f);
                timeLabel.setForeground(glow);
                repaint();
            });
            blinkTimer.start();
        }

        private void updateTime() {
            LocalDateTime now = LocalDateTime.now();
            String hh = String.format("%02d", now.getHour()%12==0?12:now.getHour()%12);
            String mm = String.format("%02d", now.getMinute());
            String ss = String.format("%02d", now.getSecond());
            String ampm = now.getHour() < 12 ? "AM" : "PM";

            // blinking colon effect (toggle every second)
            colonOn = !colonOn;
            String colon = colonOn ? ":" : " ";
            String timeText = hh + colon + mm + ":" + ss + " " + ampm;
            timeLabel.setText(timeText);
            dayLabel.setText(now.format(DateTimeFormatter.ofPattern("EEEE")) + " • " + now.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            // subtle neon glow under the time
            Graphics2D g2 = (Graphics2D) g.create();
            int w = getWidth(), h = getHeight();
            int cx = w/2, cy = h/3;
            int r = Math.min(w, h)/3;
            float phase = (float)(0.5f + 0.5f*Math.sin(glowPhase));
            Color glow = blend(NEON, NEON_ALT, 0.3f + 0.7f*phase);
            Composite orig = g2.getComposite();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.06f + 0.02f*phase));
            g2.setPaint(new RadialGradientPaint(new Point(cx, cy), r,
                    new float[]{0f, 0.6f, 1f},
                    new Color[]{glow, new Color(0,0,0,0), new Color(0,0,0,0)}));
            g2.fillOval(cx-r, cy-r, r*2, r*2);
            g2.setComposite(orig);
            g2.dispose();
        }
    }

    // ------------------ Stopwatch Panel ------------------
    private class StopwatchPanel extends JPanel {
        private JLabel display;
        private JButton startBtn, stopBtn, lapBtn, resetBtn;
        private DefaultListModel<String> lapModel;
        private javax.swing.Timer updateTimer;

        private long startTimeNano = 0L;
        private long elapsedBeforeNano = 0L;
        private boolean running = false;

        StopwatchPanel() {
            setOpaque(true);
            setBackground(BG);
            setLayout(new BorderLayout(12,12));
            setBorder(new EmptyBorder(18,18,18,18));

            JPanel top = createGlassPanel(new BorderLayout());
            top.setPreferredSize(new Dimension(760, 140));
            display = new JLabel(formatTime(0L), SwingConstants.CENTER);
            display.setFont(new Font("Digital-7 Mono", Font.BOLD, 48));
            display.setForeground(NEON_ALT);
            top.add(display, BorderLayout.CENTER);

            JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 8));
            btns.setOpaque(false);
            startBtn = neonButton("Start", NEON);
            stopBtn = neonButton("Stop", new Color(255,120,120));
            lapBtn = neonButton("Lap", NEON_ALT);
            resetBtn = neonButton("Reset", new Color(200,200,200));
            btns.add(startBtn); btns.add(stopBtn); btns.add(lapBtn); btns.add(resetBtn);
            top.add(btns, BorderLayout.SOUTH);

            add(top, BorderLayout.NORTH);

            // laps
            lapModel = new DefaultListModel<>();
            JList<String> lapList = new JList<>(lapModel);
            lapList.setBackground(new Color(12,14,20));
            lapList.setForeground(new Color(200,220,240));
            lapList.setFont(new Font("Monospaced", Font.PLAIN, 14));
            add(new JScrollPane(lapList), BorderLayout.CENTER);

            // actions
            startBtn.addActionListener(e -> startStopwatch());
            stopBtn.addActionListener(e -> stopStopwatch());
            lapBtn.addActionListener(e -> recordLap());
            resetBtn.addActionListener(e -> resetStopwatch());

            updateTimer = new javax.swing.Timer(20, e -> updateDisplay());
        }

        private void startStopwatch() {
            if (!running) {
                startTimeNano = System.nanoTime();
                running = true;
                updateTimer.start();
            }
        }

        private void stopStopwatch() {
            if (running) {
                long now = System.nanoTime();
                elapsedBeforeNano += now - startTimeNano;
                running = false;
                updateTimer.stop();
            }
        }

        private void resetStopwatch() {
            startTimeNano = 0L;
            elapsedBeforeNano = 0L;
            running = false;
            updateTimer.stop();
            display.setText(formatTime(0L));
            lapModel.clear();
        }

        private void recordLap() {
            long t = getElapsedNano();
            lapModel.add(0, String.format("Lap %02d  %s", lapModel.getSize()+1, formatTime(t)));
        }

        private void updateDisplay() {
            display.setText(formatTime(getElapsedNano()));
        }

        private long getElapsedNano() {
            if (running) {
                long now = System.nanoTime();
                return elapsedBeforeNano + (now - startTimeNano);
            } else {
                return elapsedBeforeNano;
            }
        }

        private String formatTime(long nano) {
            long ms = nano / 1_000_000;
            long centis = (ms / 10) % 100;
            long seconds = (ms / 1000) % 60;
            long minutes = (ms / (1000*60)) % 60;
            long hours = (ms / (1000*60*60));
            if (hours > 0)
                return String.format("%02d:%02d:%02d.%02d", hours, minutes, seconds, centis);
            return String.format("%02d:%02d.%02d", minutes, seconds, centis);
        }
    }

    // ------------------ Timer Panel ------------------
    private class TimerPanel extends JPanel {
        private JLabel display;
        private JButton startBtn, pauseBtn, resetBtn;
        private JProgressBar progress;
        private JComboBox<String> presetCombo;
        private JSpinner minutesSpinner, secondsSpinner;
        private javax.swing.Timer countdownTimer;

        private long totalMillis = 0;
        private long remainingMillis = 0;
        private boolean running = false;
        private long lastTick = 0;

        TimerPanel() {
            setOpaque(true);
            setBackground(BG);
            setLayout(new BorderLayout(12,12));
            setBorder(new EmptyBorder(18,18,18,18));

            JPanel top = createGlassPanel(new BorderLayout());
            top.setPreferredSize(new Dimension(760, 140));
            display = new JLabel("00:00.00", SwingConstants.CENTER);
            display.setFont(new Font("Digital-7 Mono", Font.BOLD, 48));
            display.setForeground(NEON);
            top.add(display, BorderLayout.CENTER);

            JPanel controls = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 6));
            controls.setOpaque(false);
            startBtn = neonButton("Start", NEON);
            pauseBtn = neonButton("Pause", new Color(255, 180, 60));
            resetBtn = neonButton("Reset", new Color(200,200,200));
            controls.add(startBtn); controls.add(pauseBtn); controls.add(resetBtn);

            top.add(controls, BorderLayout.SOUTH);
            add(top, BorderLayout.NORTH);

            JPanel mid = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 6));
            mid.setOpaque(false);
            mid.add(new JLabel("Preset:"));
            presetCombo = new JComboBox<>(new String[]{"1 min","3 min","5 min","10 min","Custom"});
            mid.add(presetCombo);
            mid.add(new JLabel("Min:"));
            minutesSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 999, 1));
            mid.add(minutesSpinner);
            mid.add(new JLabel("Sec:"));
            secondsSpinner = new JSpinner(new SpinnerNumberModel(30, 0, 59, 1));
            mid.add(secondsSpinner);
            add(mid, BorderLayout.CENTER);

            progress = new JProgressBar(0, 1000);
            progress.setStringPainted(true);
            add(progress, BorderLayout.SOUTH);

            // Preset actions
            presetCombo.addActionListener(e -> {
                String s = (String)presetCombo.getSelectedItem();
                if (s.equals("1 min")) { minutesSpinner.setValue(1); secondsSpinner.setValue(0); }
                else if (s.equals("3 min")) { minutesSpinner.setValue(3); secondsSpinner.setValue(0); }
                else if (s.equals("5 min")) { minutesSpinner.setValue(5); secondsSpinner.setValue(0); }
                else if (s.equals("10 min")) { minutesSpinner.setValue(10); secondsSpinner.setValue(0); }
            });

            startBtn.addActionListener(e -> startTimer());
            pauseBtn.addActionListener(e -> pauseTimer());
            resetBtn.addActionListener(e -> resetTimer());

            countdownTimer = new javax.swing.Timer(50, e -> tick());
        }

        private void startTimer() {
            if (!running) {
                if (remainingMillis <= 0) {
                    int mins = (Integer) minutesSpinner.getValue();
                    int secs = (Integer) secondsSpinner.getValue();
                    totalMillis = (mins*60L + secs) * 1000L;
                    remainingMillis = totalMillis;
                    if (totalMillis <= 0) {
                        JOptionPane.showMessageDialog(this, "Set a duration > 0");
                        return;
                    }
                }
                running = true;
                lastTick = System.nanoTime();
                countdownTimer.start();
            }
        }

        private void pauseTimer() {
            if (running) {
                countdownTimer.stop();
                running = false;
            }
        }

        private void resetTimer() {
            countdownTimer.stop();
            running = false;
            remainingMillis = 0;
            totalMillis = 0;
            progress.setValue(0);
            display.setText("00:00.00");
        }

        private void tick() {
            long now = System.nanoTime();
            long delta = (now - lastTick) / 1_000_000; // ms
            lastTick = now;
            remainingMillis -= delta;
            if (remainingMillis <= 0) {
                remainingMillis = 0;
                countdownTimer.stop();
                running = false;
                display.setText("00:00.00");
                progress.setValue(progress.getMaximum());
                // alarm sound
                new Thread(() -> playTone(800, 1200, 250)).start();
                JOptionPane.showMessageDialog(this, "Timer finished!");
                return;
            }
            // update UI
            display.setText(formatTimer(remainingMillis));
            if (totalMillis > 0) {
                int val = (int) ((1.0 - (double) remainingMillis / totalMillis) * progress.getMaximum());
                progress.setValue(Math.max(0, Math.min(progress.getMaximum(), val)));
                progress.setString(String.format("%d%%", (int)(100.0*val/progress.getMaximum())));
            }
        }

        private String formatTimer(long ms) {
            long centis = (ms / 10) % 100;
            long seconds = (ms / 1000) % 60;
            long minutes = (ms / (1000*60));
            return String.format("%02d:%02d.%02d", minutes, seconds, centis);
        }
    }

    // ------------------ Alarm Panel ------------------
    private class AlarmPanel extends JPanel {
        private DefaultListModel<String> alarmModel;
        private JList<String> alarmList;
        private JTextField timeField; // HH:mm
        private JButton addBtn, removeBtn, enableBtn, disableBtn;
        private final List<AlarmItem> alarms = new CopyOnWriteArrayList<>();
        private javax.swing.Timer checkTimer;

        AlarmPanel() {
            setOpaque(true);
            setBackground(BG);
            setLayout(new BorderLayout(12,12));
            setBorder(new EmptyBorder(18,18,18,18));

            JPanel top = createGlassPanel(new GridBagLayout());
            top.setPreferredSize(new Dimension(760, 120));
            GridBagConstraints g = new GridBagConstraints();
            g.insets = new Insets(8,8,8,8);
            g.gridx = 0; g.gridy = 0;
            top.add(new JLabel("Alarm Time (HH:mm)"), g);
            timeField = new JTextField(6);
            timeField.setText(LocalTime.now().plusMinutes(1).format(HOUR_MIN_FMT));
            g.gridx = 1;
            top.add(timeField, g);
            addBtn = neonButton("Add Alarm", NEON);
            g.gridx = 2;
            top.add(addBtn, g);

            add(top, BorderLayout.NORTH);

            alarmModel = new DefaultListModel<>();
            alarmList = new JList<>(alarmModel);
            alarmList.setFont(new Font("Monospaced", Font.PLAIN, 14));
            alarmList.setBackground(new Color(10,12,16));
            alarmList.setForeground(new Color(200,220,240));
            add(new JScrollPane(alarmList), BorderLayout.CENTER);

            JPanel right = new JPanel(new GridLayout(4,1,8,8));
            right.setOpaque(false);
            removeBtn = neonButton("Remove Selected", new Color(255,120,120));
            enableBtn = neonButton("Enable", NEON);
            disableBtn = neonButton("Disable", new Color(200,200,200));
            right.add(enableBtn); right.add(disableBtn); right.add(removeBtn);
            add(right, BorderLayout.EAST);

            // actions
            addBtn.addActionListener(e -> addAlarm());
            removeBtn.addActionListener(e -> removeSelectedAlarm());
            enableBtn.addActionListener(e -> setSelectedAlarmEnabled(true));
            disableBtn.addActionListener(e -> setSelectedAlarmEnabled(false));

            // load any pre-saved? (not persisting in this demo)
            // periodic check
            checkTimer = new javax.swing.Timer(1000, e -> checkAlarms());
            checkTimer.start();
        }

        private void addAlarm() {
            String txt = timeField.getText().trim();
            try {
                LocalTime t = LocalTime.parse(txt, HOUR_MIN_FMT);
                AlarmItem ai = new AlarmItem(t, true, UUID.randomUUID().toString());
                alarms.add(ai);
                alarmModel.addElement(ai.toDisplay());
                JOptionPane.showMessageDialog(this, "Alarm added for " + t.format(HOUR_MIN_FMT));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid time format. Use HH:mm (24-hour). Example: 07:30 or 19:45");
            }
        }

        private void removeSelectedAlarm() {
            int i = alarmList.getSelectedIndex();
            if (i == -1) return;
            alarms.remove(i);
            alarmModel.remove(i);
        }

        private void setSelectedAlarmEnabled(boolean en) {
            int i = alarmList.getSelectedIndex();
            if (i == -1) return;
            AlarmItem ai = alarms.get(i);
            ai.enabled = en;
            alarmModel.set(i, ai.toDisplay());
        }

        private void checkAlarms() {
            LocalTime now = LocalTime.now().withSecond(0).withNano(0);
            for (AlarmItem ai : alarms) {
                if (!ai.triggeredToday && ai.enabled && ai.time.equals(now)) {
                    // trigger
                    ai.triggeredToday = true;
                    // show notification and play tone
                    new Thread(() -> {
                        // play multi-tone pattern
                        for (int k=0;k<3;k++) {
                            playTone(880 + k*120, 1200, 350);
                            try { Thread.sleep(250); } catch (InterruptedException ex) {}
                        }
                        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(AlarmPanel.this, "Alarm! " + ai.time.format(HOUR_MIN_FMT)));
                    }).start();
                }
                // reset triggeredToday at midnight
                if (LocalTime.now().isBefore(LocalTime.of(0,1))) {
                    ai.triggeredToday = false;
                }
            }
            // update list display (for visual status)
            SwingUtilities.invokeLater(() -> {
                alarmModel.clear();
                for (AlarmItem ai : alarms) alarmModel.addElement(ai.toDisplay());
            });
        }

        class AlarmItem {
            LocalTime time;
            boolean enabled;
            boolean triggeredToday = false;
            String id;
            AlarmItem(LocalTime t, boolean e, String id) { time = t; enabled = e; this.id = id; }
            String toDisplay() {
                return String.format("%s  [%s]  %s", time.format(HOUR_MIN_FMT), enabled ? "ENABLED " : "DISABLED", triggeredToday ? "(triggered today)" : "");
            }
        }
    }

    // -------------------- Utilities --------------------

    // create a frosted glass panel style
    private JPanel createGlassPanel(LayoutManager lm) {
        JPanel p = new JPanel(lm) {
            @Override
            protected void paintComponent(Graphics g) {
                // translucent rounded rectangle
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int arc = 18;
                g2.setColor(PANEL);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
                // inner border glow
                g2.setColor(new Color(0,0,0,60));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1,1,getWidth()-3,getHeight()-3,arc,arc);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(12,12,12,12));
        return p;
    }

    // create neon styled JButton
    private JButton neonButton(String text, Color accent) {
        JButton b = new JButton(text);
        b.setFont(new Font("SansSerif", Font.BOLD, 14));
        b.setForeground(Color.WHITE);
        b.setBackground(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 220));
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(8,14,8,14));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { b.setBackground(accent.darker()); }
            @Override
            public void mouseExited(MouseEvent e) { b.setBackground(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 220)); }
        });
        return b;
    }

    // blend two colors (simple)
    private static Color blend(Color a, Color b, float ratio) {
        ratio = Math.max(0f, Math.min(1f, ratio));
        int r = (int) (a.getRed() * (1 - ratio) + b.getRed() * ratio);
        int g = (int) (a.getGreen() * (1 - ratio) + b.getGreen() * ratio);
        int bl = (int) (a.getBlue() * (1 - ratio) + b.getBlue() * ratio);
        return new Color(Math.max(0, Math.min(255,r)), Math.max(0, Math.min(255,g)), Math.max(0, Math.min(255,bl)));
    }

    // play a sine tone (frequencyHz) for durationMs at given sampleRate; non-blocking wrapper recommended
    private static void playTone(int frequencyHz, int sampleRate, int durationMs) {
        try {
            byte[] buf = createSineWaveBuffer(frequencyHz, sampleRate, durationMs);
            AudioFormat af = new AudioFormat(sampleRate, 8, 1, true, false);
            try (SourceDataLine sdl = AudioSystem.getSourceDataLine(af)) {
                sdl.open(af);
                sdl.start();
                sdl.write(buf, 0, buf.length);
                sdl.drain();
            }
        } catch (LineUnavailableException e) {
            Toolkit.getDefaultToolkit().beep();
        } catch (Throwable t) {
            Toolkit.getDefaultToolkit().beep();
        }
    }

    private static byte[] createSineWaveBuffer(int freq, int sampleRate, int ms) {
        int samples = (ms * sampleRate) / 1000;
        byte[] output = new byte[samples];
        double period = (double)sampleRate / freq;
        for (int i = 0; i < output.length; i++) {
            double angle = 2.0 * Math.PI * i / period;
            output[i] = (byte)(Math.sin(angle) * 127f);
        }
        return output;
    }

    // format stopwatch time - helper used in stopwatch inner class too
    private static String formatTimeShort(long nano) {
        long ms = nano / 1_000_000;
        long centis = (ms / 10) % 100;
        long seconds = (ms / 1000) % 60;
        long minutes = (ms / (1000*60));
        return String.format("%02d:%02d.%02d", minutes, seconds, centis);
    }

    // ---------------- Main ----------------
    public static void main(String[] args) {
        // ensure UI created on EDT
        SwingUtilities.invokeLater(() -> {
            try {
                // optional: set a dark look and feel if supported
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            NeonClockApp app = new NeonClockApp();
            app.setVisible(true);
        });
    }
}
