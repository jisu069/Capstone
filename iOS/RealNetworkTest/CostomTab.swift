import SwiftUI

struct CostomTab: View {
    var body: some View {
        TabView {
            ContentView().tabItem {
                Image(systemName: "map")
                Text("지도")
            }
            FavoriteView().tabItem {
                Image(systemName: "star")
                Text("즐겨찾기")
            }
            SettingView().tabItem {
                Image(systemName: "gearshape")
                Text("설정")
            }
        }
    }
}

struct CostomTab_Previews: PreviewProvider {
    static var previews: some View {
        CostomTab()
    }
}
