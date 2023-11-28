import SwiftUI
import NMapsMap
import CoreLocation
import Foundation

var cctvmarker: [NMFMarker] = []
var nextNodeIdx: Int = 1
struct PathMapView: UIViewRepresentable {
    @Binding var path: [[Point]]?
    @Binding var startPoint: Point?
    @Binding var endPoint: Point?
    @Binding var selectPath: Int
    @Binding var isStartNevi: Bool
    @StateObject var neviInfo = NeviInfo()
    @StateObject var locationManager = LocationManager()
    @State private var pathOverlay = [NMFPath(),NMFPath(),NMFPath(),NMFPath()]
    @State private var startMarker: NMFMarker = NMFMarker()
    @Binding var direction: [Int]?
    
    func makeUIView(context: Context) -> NMFNaverMapView {
        let view = NMFNaverMapView()
        //        let startcam: NMGLatLng = NMGLatLng(lat: ((startPoint?.lat ?? 0)+(endPoint?.lat ?? 0))/2, lng: ((startPoint?.lon ?? 0)+(endPoint?.lon ?? 0))/2)
        //        let cam = NMFCameraUpdate(scrollTo: startcam)
        //        view.mapView.moveCamera(cam)
        view.showCompass = true
        view.showLocationButton = true
        view.showZoomControls = true
        view.mapView.positionMode = .direction
        view.mapView.zoomLevel = 13
        view.mapView.isIndoorMapEnabled = true
        
        return view
    }
    
    func updateUIView(_ uiView: NMFNaverMapView, context: Context) {
        let startcam: NMGLatLng = NMGLatLng(lat: ((startPoint?.lat ?? 0)+(endPoint?.lat ?? 0))/2, lng: ((startPoint?.lon ?? 0)+(endPoint?.lon ?? 0))/2)
        var cam = NMFCameraUpdate(scrollTo: startcam)
        uiView.mapView.moveCamera(cam)
        var lst: [[NMGLatLng]] = []
        if let path {
            for c in path {
                var l: [NMGLatLng] = []
                for d in c {
                    if l.isEmpty || (l.last?.lat != d.lat && l.last?.lng != d.lon) {
                        l.append(NMGLatLng(lat: d.lat, lng: d.lon))
                    }
                }
                lst.append(l)
            }
        }
        
        if lst.count != 0{
            //대로변
            pathOverlay[0].path = NMGLineString(points:lst[0])
            pathOverlay[0].color = UIColor.green
            
            //최단거리
            pathOverlay[1].path = NMGLineString(points:lst[1])
            pathOverlay[1].color = UIColor.red
            
            //cctv경로
            pathOverlay[2].path = NMGLineString(points:lst[2])
            pathOverlay[2].color = UIColor.yellow
            
            //테스트 경로
            pathOverlay[3].path = NMGLineString(points: lst[4])
            pathOverlay[3].color = UIColor.orange
            
            for over in pathOverlay {
                over.zIndex = 1
                over.mapView = uiView.mapView
            }
            
            
            for i in cctvmarker {
                i.mapView = nil
            }
            
            cctvmarker = []
            for i in lst[3]{
                let cctv = NMFMarker()
                cctv.position = i
                cctv.width = 15
                cctv.height = 15
                cctv.iconTintColor = UIColor.blue
                cctvmarker.append(cctv)
            }
            
        }
        
        switch selectPath {
        case 1:
            pathOverlay[0].color = UIColor.blue
            pathOverlay[0].zIndex = 100
        case 2:
            pathOverlay[1].color = UIColor.blue
            pathOverlay[1].zIndex = 100
        case 3:
            pathOverlay[2].color = UIColor.blue
            pathOverlay[2].zIndex = 100
            for i in cctvmarker {
                i.mapView = uiView.mapView
            }
            
        case 5:
            pathOverlay[3].color = UIColor.blue
            pathOverlay[3].zIndex = 100
        default:
            selectPath = 0
        }
        
        startPoint = Point(lon: locationManager.lastLocation?.coordinate.longitude ?? 0, lat: locationManager.lastLocation?.coordinate.latitude ?? 0)
        
        var startCoor: NMGLatLng? = nil
        var endCoor: NMGLatLng? = nil
        
        
        var endMarker: NMFMarker = NMFMarker()
        
        if let endPoint {
            endCoor = NMGLatLng(lat: endPoint.lat, lng: endPoint.lon)
            endMarker.iconImage = NMF_MARKER_IMAGE_RED
        }
        
        if let startPoint {
            startCoor = NMGLatLng(lat: startPoint.lat, lng: startPoint.lon)
            startMarker.iconImage = NMF_MARKER_IMAGE_GREEN
        }
        
        if let startCoor {
            startMarker.position = startCoor
            startMarker.mapView = uiView.mapView
        }
        
        if let endCoor {
            endMarker.position = endCoor
            endMarker.mapView = uiView.mapView
        }
        
        if isStartNevi == true {
            print(direction)
            for i in 0..<pathOverlay.count{
                if i != selectPath-1{
                    pathOverlay[i].mapView = nil
                }
            }
            startMarker.mapView = nil
            cam = NMFCameraUpdate(scrollTo: startCoor!,zoomTo: 17.0)
            uiView.mapView.moveCamera(cam)
            
//            var distance = sqrt(pow(lst[selectPath-1][nextNodeIdx].lat-startPoint!.lat,2)+pow(lst[selectPath-1][nextNodeIdx].lng-startPoint!.lon,2))
//            
//            if distance == 0.0001{
//                nextNodeIdx = nextNodeIdx+1
//            }
        }
    }
}
