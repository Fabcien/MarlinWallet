/**
 * eCash Wallet App using WebView
 * This approach uses a web-based wallet with full WebAssembly support
 * embedded in a React Native WebView
 */

import React, {useState, useRef, useEffect} from 'react';
import {
  SafeAreaView,
  StatusBar,
  StyleSheet,
  View,
  Alert,
  Dimensions,
  BackHandler,
  NativeModules,
} from 'react-native';
import {WebView} from 'react-native-webview';
import ReactNativeHapticFeedback from 'react-native-haptic-feedback';
import * as Keychain from 'react-native-keychain';

// Interface for messages to and from the WebView
interface WebViewMessage {
  type: string;
  data?: any;
}

function App(): React.JSX.Element {
  const webViewRef = useRef<WebView>(null);

  // Properly terminate the app if the user cancels the authentication. Without
  // this, the app will stay running in the background and can be resumed in an
  // inconsistent state.
  function killApp() {
    try {
      // Use native module to properly kill the app
      NativeModules.AppKiller.killApp();
    } catch (error) {
      console.log('Failed to kill app with native module, falling back to BackHandler:', error);
      BackHandler.exitApp();
    }
  }

  // Send message to WebView
  const sendMessageToWebView = (message: WebViewMessage) => {
    if (webViewRef.current) {
      webViewRef.current.postMessage(JSON.stringify(message));
    } else {
      console.error('WebView ref is null, cannot send message');
    }
  };

  // Trigger haptic feedback for transaction finalization
  const triggerTransactionHaptic = () => {
    try {
      ReactNativeHapticFeedback.trigger('impactMedium', {
        enableVibrateFallback: true,
        ignoreAndroidSystemSettings: false,
      });
    } catch (error) {
      console.error('Haptic feedback error:', error);
    }
  };

  // Store mnemonic in secure keychain storage
  const storeMnemonic = async (mnemonic: string): Promise<void> => {
    let closeWalletMessage = 'Close the wallet';
    try {
      await Keychain.setGenericPassword('eCashWallet', mnemonic, {
        accessControl: Keychain.ACCESS_CONTROL.BIOMETRY_ANY,
        accessGroup: undefined,
        authenticationPrompt: {
          title: 'Authenticate to store your wallet private key',
          description: 'This is required to store your private key to the device secure storage',
          cancel: closeWalletMessage,
        },
        service: 'eCashWallet',
      });
    } catch (error) {
      console.log('storeMnemonic: Error storing mnemonic in keychain:', error);
      killApp();
      return;
    }
  };

  // Load mnemonic from secure keychain storage
  const loadMnemonic = async (): Promise<string | null> => {
    // TODO Fallback to watch-only mode if the user cancels the authentication
    // const watchOnlyMessage = 'Open in watch-only mode';
    const watchOnlyMessage = 'Close the wallet';
    try {
      const credentials = await Keychain.getGenericPassword({
        authenticationPrompt: {
          title: 'Authenticate to access your wallet',
          description: 'This is required to load your private key from the secure storage',
          cancel: watchOnlyMessage,
        },
        service: 'eCashWallet',
      });
      
      if (credentials && credentials.password) {
        console.log('Mnemonic loaded from keychain');
        return credentials.password;
      } else {
        console.log('No mnemonic found in keychain');
        return null;
      }
    } catch (error) {
      // TODO check the cancellation message for using watch-only mode
      // const message: string = (error as any).message;
      // if (message.includes(watchOnlyMessage)) {
      //   console.log('User chose to open in watch-only mode');
      //   return Promise.reject(error);
      // }

      console.log('Error loading mnemonic from keychain:', error);
      killApp();
      return Promise.reject(error);
    }
  };

  // Handle messages from the WebView
  const handleWebViewMessage = (event: any) => {
    try {
      const message: WebViewMessage = JSON.parse(event.nativeEvent.data);

      switch (message.type) {
        case 'LOG':
          console.log('WebView: ', message.data);
          break;

        case 'CLOSE_APP':
          console.log('Killing app due to failed biometric authentication');
          killApp();
          break;

        case 'TX_FINALIZED':
          console.log('Transaction finalized, triggering haptic feedback');
          triggerTransactionHaptic();
          break;

        case 'STORE_MNEMONIC':
          if (message.data) {
            storeMnemonic(message.data);
          }
          break;

        case 'LOAD_MNEMONIC':
          loadMnemonic().then((mnemonic) => {
            sendMessageToWebView({
              type: 'MNEMONIC_RESPONSE',
              data: mnemonic
            });
          }).catch((error) => {
            console.log('Error in loadMnemonic:', error);
          });
          break;

        default:
          // Unknown message type - log and ignore
          console.log('Unknown WebView message type:', message.type);
          break;
      }
    } catch (error) {
      console.error('Failed to parse WebView message:', error);
    }
  };

  // Get the local HTML file path
  const getLocalHtmlPath = () => {
    // For development, we'll use a local file
    // In production, you might want to serve this from a web server
    return 'file:///android_asset/web/index.html';
  };

  return (
    <SafeAreaView style={styles.container}>
      <StatusBar barStyle="light-content" backgroundColor="#667eea" />
      
      <View style={styles.webViewContainer}>
        <WebView
          ref={webViewRef}
          source={{uri: getLocalHtmlPath()}}
          style={styles.webView}
          onMessage={handleWebViewMessage}
          javaScriptEnabled={true}
          domStorageEnabled={true}
          startInLoadingState={true}
          scalesPageToFit={true}
          mixedContentMode="compatibility"
          onError={(syntheticEvent) => {
            const {nativeEvent} = syntheticEvent;
            console.error('WebView error:', nativeEvent);
            Alert.alert('WebView Error', 'Failed to load the wallet interface');
          }}
          onHttpError={(syntheticEvent) => {
            const {nativeEvent} = syntheticEvent;
            console.error('WebView HTTP error:', nativeEvent);
          }}
        />
      </View>
    </SafeAreaView>
  );
}

const {width, height} = Dimensions.get('window');
const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#667eea',
  },
  webViewContainer: {
    flex: 1,
    backgroundColor: '#667eea',
  },
  webView: {
    flex: 1,
    width: width,
    height: height,
  },
});

export default App;
