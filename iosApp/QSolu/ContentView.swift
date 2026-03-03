import SwiftUI
import sharedUIKit

struct ContentView: View {
    var body: some View {
        SharedHomeComposableView()
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            .ignoresSafeArea(.all)
            .background(Color(uiColor: .systemBackground))
    }
}

struct SharedHomeComposableView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        let host = IosProductListComposeHost()
        let controller = host.viewController()
        controller.view.backgroundColor = .systemBackground
        return controller
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
    }
}

#Preview {
    ContentView()
}
