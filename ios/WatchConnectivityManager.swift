import Foundation
import WatchConnectivity
import React

@objc(WatchConnectivityModule)
class WatchConnectivityModule: NSObject, RCTBridgeModule {
    
    override init() {
        super.init()
        NSLog("[WatchSync] WatchConnectivityModule initialized")
    }
    
    static func moduleName() -> String! {
        return "WatchConnectivity"
    }
    
    static func requiresMainQueueSetup() -> Bool {
        return true
    }
    
    @objc(sendDataToWatch:bip21Prefix:)
    func sendDataToWatch(_ address: String, bip21Prefix: String) {
        NSLog("[WatchSync] sendDataToWatch called with address: \(address), prefix: \(bip21Prefix)")
        
        guard WCSession.isSupported() else {
            NSLog("[WatchSync] WCSession not supported")
            return
        }
        
        let session = WCSession.default
        NSLog("[WatchSync] Session activation state: \(session.activationState.rawValue)")
        NSLog("[WatchSync] Session isPaired: \(session.isPaired)")
        NSLog("[WatchSync] Session isWatchAppInstalled: \(session.isWatchAppInstalled)")
        
        // Send data using application context (persistent, even if watch isn't reachable)
        // Include timestamp to force update even if address didn't change
        let context: [String: Any] = [
            "address": address,
            "bip21_prefix": bip21Prefix,
            "timestamp": Date().timeIntervalSince1970
        ]
        
        NSLog("[WatchSync] About to send context: \(context)")
        
        do {
            try session.updateApplicationContext(context)
            NSLog("[WatchSync] Successfully sent context to watch")
        } catch {
            NSLog("[WatchSync] Failed to send data to watch: \(error.localizedDescription)")
        }
    }
}

