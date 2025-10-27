import SwiftUI

struct ContentView: View {
    @StateObject private var connectivityManager = WatchConnectivityManager.shared
    
    var body: some View {
        if let walletData = connectivityManager.walletData {
            // Show QR code with wallet address
            QRCodeView(walletData: walletData)
        } else {
            // Show "open on phone" screen
            OpenOnPhoneView {
                connectivityManager.openPhoneApp()
            }
        }
    }
}

struct QRCodeView: View {
    let walletData: WalletData
    
    var body: some View {
        ZStack {
            Color.black.edgesIgnoringSafeArea(.all)
            
            VStack(spacing: 8) {
                // eCash logo at top (fixed height)
                ZStack {
                    Circle()
                        .fill(Color(red: 0.0, green: 0.48, blue: 0.89))
                        .frame(width: 18, height: 18)
                    
                    Image("Logo")
                        .resizable()
                        .aspectRatio(contentMode: .fit)
                        .frame(width: 18, height: 18)
                        .clipShape(Circle())
                }
                
                // QR Code - takes all remaining space
                GeometryReader { geometry in
                    let qrSize = min(geometry.size.width, geometry.size.height)
                    
                    SimpleQRCodeView(
                        content: createBip21Uri(),
                        size: qrSize
                    )
                    .frame(width: geometry.size.width, height: geometry.size.height)
                }
            }
            .padding(4)
        }
    }
    
    private func createBip21Uri() -> String {
        // Strip prefix from address if present
        let prefixlessAddress = walletData.address.contains(":") 
            ? String(walletData.address.split(separator: ":")[1])
            : walletData.address
        
        return "\(walletData.bip21Prefix)\(prefixlessAddress)"
    }
}

#Preview {
    ContentView()
}

