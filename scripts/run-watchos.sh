#!/bin/bash

set -e

cd "$(dirname "$0")/../ios"

# Check for connected real Apple Watch first
REAL_WATCH=$(xcrun xctrace list devices 2>&1 | grep "Apple Watch" | grep -v "Simulator" | head -1 | grep -o '[A-F0-9-]\{8\}-[A-F0-9-]\{36\}' || echo "")

if [ -n "$REAL_WATCH" ]; then
    echo "⌚ Found connected Apple Watch: $REAL_WATCH"
    DEVICE_ID="$REAL_WATCH"
    SDK="watchos"
    PLATFORM="watchOS"
    IS_SIMULATOR=false
    
    echo "🔨 Building for real Apple Watch..."
    xcodebuild -workspace eCashWalletApp.xcworkspace \
      -scheme eCashWalletWatch \
      -sdk watchos \
      -configuration Debug \
      -destination "platform=watchOS,id=$DEVICE_ID" \
      build
    
    echo "📦 Installing to Apple Watch..."
    # For real device, xcodebuild installs automatically with -destination
    # Just need to find and run the app
    APP_PATH=$(find ~/Library/Developer/Xcode/DerivedData/eCashWalletApp-*/Build/Products/Debug-watchos -name "eCashWalletWatch.app" -type d | head -1)
    
    if [ -z "$APP_PATH" ]; then
        echo "❌ Could not find built app"
        exit 1
    fi
    
    echo "✅ App installed! Launch it manually from the Apple Watch."
    exit 0
fi

# Fall back to simulator
# Try to find a booted watch simulator
BOOTED_WATCH=$(xcrun simctl list devices booted | grep "Watch" | head -1 | grep -o '[A-F0-9-]\{36\}' || echo "")

if [ -n "$BOOTED_WATCH" ]; then
    echo "📱 Found booted watch simulator: $BOOTED_WATCH"
    DEVICE_ID="$BOOTED_WATCH"
else
    # Find any available watch simulator
    DEVICE_ID=$(xcrun simctl list devices available | grep "Watch" | head -1 | grep -o '[A-F0-9-]\{36\}' || echo "")
    
    if [ -z "$DEVICE_ID" ]; then
        echo "❌ No watch simulator found"
        exit 1
    fi
    
    echo "📱 Found watch simulator: $DEVICE_ID"
    echo "🔄 Booting simulator..."
    xcrun simctl boot "$DEVICE_ID" 2>/dev/null || true
    open -a Simulator
    sleep 2
fi

echo "🔨 Building watchOS app..."
xcodebuild -workspace eCashWalletApp.xcworkspace \
  -scheme eCashWalletWatch \
  -sdk watchsimulator \
  -configuration Debug \
  -destination "platform=watchOS Simulator,id=$DEVICE_ID" \
  build

echo "📦 Installing app..."
APP_PATH=$(find ~/Library/Developer/Xcode/DerivedData/eCashWalletApp-*/Build/Products/Debug-watchsimulator -name "eCashWalletWatch.app" -type d | head -1)

if [ -z "$APP_PATH" ]; then
    echo "❌ Could not find built app"
    exit 1
fi

xcrun simctl install "$DEVICE_ID" "$APP_PATH"

echo "🚀 Launching app..."
# Get the actual bundle ID from the built app
BUNDLE_ID=$(/usr/libexec/PlistBuddy -c "Print CFBundleIdentifier" "$APP_PATH/Info.plist")
echo "Bundle ID: $BUNDLE_ID"
xcrun simctl launch "$DEVICE_ID" "$BUNDLE_ID"

echo "✅ watchOS app launched successfully!"

