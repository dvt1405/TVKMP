import SwiftUI
import shared

struct ContentView: View {
	let greet = Greeting().greet()
    @State private var iptvUrl: String = ""
//    @FocusState private var focusedIPTV: Bool = false

	var body: some View {
        VStack() {
            TextField("IPTV Url", text: $iptvUrl)
                .frame(height: 60)
                .textFieldStyle(PlainTextFieldStyle())
                .cornerRadius(10)
                .padding([.trailing, .leading], 16)
                .overlay(RoundedRectangle(cornerRadius: 10)
                    .stroke(Color.gray)
                    .padding([.trailing, .leading], 2))
                .disableAutocorrection(true)
            Text(iptvUrl)
        }
	}
}

struct ContentView_Previews: PreviewProvider {
	static var previews: some View {
		ContentView()
	}
}
