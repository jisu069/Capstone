import SwiftUI

struct FavoriteView: View {
    @State var path: Bool = true
    var body: some View {
        NavigationView{
            VStack(spacing:0){
                HStack{
                    Button(action: {path = true}){
                        Text("목적지")
                            .frame(maxWidth: .infinity)
                    }
                    .frame(height: 40.0)
            
                    Button(action: {path = false}){
                        Text("경로")
                            .frame(maxWidth: .infinity)
                    }
                    .frame(height: 40.0)
                }
                .padding()
                List{
                    if path{
                        Text("목적지")
                    }
                    else{
                        Text("경로")
                        
                    }
                }
            }
            .toolbar{
                Button(action:{}){
                    Image(systemName: "plus")
                }
            }
        }
    }
}

struct FavoriteView_Previews: PreviewProvider {
    static var previews: some View {
        FavoriteView()
    }
}
