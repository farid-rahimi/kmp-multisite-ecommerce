import SwiftUI
import sharedUIKit

struct ContentView: View {
    var body: some View {
        SharedHomeComposableView()
    }
}

struct SharedHomeComposableView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        let host = IosProductListComposeHost()
        return host.viewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
    }
}

#Preview {
    ContentView()
}
