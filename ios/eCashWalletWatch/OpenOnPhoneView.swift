import SwiftUI

struct OpenOnPhoneView: View {
    let onTap: () -> Void
    
    var body: some View {
        VStack(spacing: 12) {
            Image(systemName: "iphone")
                .font(.system(size: 48))
                .foregroundColor(.white)
            
            Text("Open the phone app\nto initialize")
                .font(.system(size: 14))
                .foregroundColor(.white)
                .multilineTextAlignment(.center)
        }
        .onTapGesture {
            onTap()
        }
    }
}

#Preview {
    OpenOnPhoneView(onTap: {})
}

