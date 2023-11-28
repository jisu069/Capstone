import SwiftUI

struct SettingView: View {
    @State var ttsToggle: Bool = true
    @State var backToggle: Bool = true
    var body: some View {
        List{
            Section(header: Text("지도API선택")){
               Text("네이버 지도")
            }
            Section(header: Text("즐겨찾기파일관리")){
                Text("즐겨찾기 파일 가져오기")
                Text("즐겨찾기 파일 내보내기")
            }
            Section(header: Text("경로안내 설정")){
                Toggle("TTS 안내", isOn: $ttsToggle)
                Toggle("백그라운드 안내", isOn: $backToggle)
            }
        }
    }
}

struct SettingView_Previews: PreviewProvider {
    static var previews: some View {
        SettingView()
    }
}
