// lib/main.dart
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:home_widget/home_widget.dart';

// ──────────────────────────────────────────────────────────────
// Entry point
// ──────────────────────────────────────────────────────────────
void main() {
  WidgetsFlutterBinding.ensureInitialized();
  // Tell home_widget which app group / provider to use on Android.
  HomeWidget.setAppGroupId('com.example.lockwidgetapp');
  runApp(const LockWidgetApp());
}

// ──────────────────────────────────────────────────────────────
// App root
// ──────────────────────────────────────────────────────────────
class LockWidgetApp extends StatelessWidget {
  const LockWidgetApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Lock Widget',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.indigo),
        useMaterial3: true,
      ),
      home: const HomePage(),
    );
  }
}

// ──────────────────────────────────────────────────────────────
// Home page
// ──────────────────────────────────────────────────────────────
class HomePage extends StatefulWidget {
  const HomePage({super.key});

  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  // Method channel that talks to the native Kotlin side
  static const _channel = MethodChannel('com.example.lockwidgetapp/lock');

  bool _isAdminActive = false;

  @override
  void initState() {
    super.initState();
    _checkAdminStatus();
    // Listen for widget taps that launch the app
    HomeWidget.widgetClicked.listen(_onWidgetClicked);
  }

  // Called when the home-screen widget button is tapped
  void _onWidgetClicked(Uri? uri) {
    if (uri?.host == 'lock') {
      _lockScreen();
    }
  }

  Future<void> _checkAdminStatus() async {
    try {
      final active = await _channel.invokeMethod<bool>('isAdminActive') ?? false;
      setState(() => _isAdminActive = active);
    } on PlatformException catch (e) {
      debugPrint('checkAdminStatus error: ${e.message}');
    }
  }

  Future<void> _requestAdminPrivileges() async {
    try {
      await _channel.invokeMethod('requestAdmin');
      // Re-check after the user returns from the system settings screen
      await Future.delayed(const Duration(seconds: 1));
      await _checkAdminStatus();
    } on PlatformException catch (e) {
      debugPrint('requestAdmin error: ${e.message}');
    }
  }

  Future<void> _lockScreen() async {
    if (!_isAdminActive) {
      _showSnack('Grant Device Admin permission first.');
      return;
    }
    try {
      await _channel.invokeMethod('lockScreen');
    } on PlatformException catch (e) {
      _showSnack('Lock failed: ${e.message}');
    }
  }

  void _showSnack(String msg) {
    ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(msg)));
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Lock Widget App'),
        centerTitle: true,
      ),
      body: Padding(
        padding: const EdgeInsets.all(24),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            // Status card
            Card(
              child: Padding(
                padding: const EdgeInsets.all(16),
                child: Row(
                  children: [
                    Icon(
                      _isAdminActive ? Icons.verified_user : Icons.gpp_bad,
                      color: _isAdminActive ? Colors.green : Colors.red,
                      size: 32,
                    ),
                    const SizedBox(width: 12),
                    Expanded(
                      child: Text(
                        _isAdminActive
                            ? 'Device Admin: Active ✓'
                            : 'Device Admin: Not granted',
                        style: Theme.of(context).textTheme.titleMedium,
                      ),
                    ),
                  ],
                ),
              ),
            ),
            const SizedBox(height: 24),

            // Grant admin button
            if (!_isAdminActive)
              ElevatedButton.icon(
                icon: const Icon(Icons.security),
                label: const Text('Grant Device Admin'),
                onPressed: _requestAdminPrivileges,
              ),

            const SizedBox(height: 12),

            // Lock now button
            FilledButton.icon(
              icon: const Icon(Icons.lock_outline),
              label: const Text('Lock Screen Now'),
              onPressed: _isAdminActive ? _lockScreen : null,
            ),

            const SizedBox(height: 32),
            const Divider(),
            const SizedBox(height: 16),

            Text(
              'Add the Lock Widget to your home screen for one-tap locking '
              'without opening the app.',
              textAlign: TextAlign.center,
              style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                    color: Colors.grey.shade600,
                  ),
            ),
          ],
        ),
      ),
    );
  }
}
