import SwiftUI
import NMapsMap
import CoreLocation
import Foundation

struct UIMapView: UIViewRepresentable {
    @Binding var startPoint: Point?
    @Binding var endPoint: Point?
    @StateObject var locationManager = LocationManager()
    @State private var marker: NMFMarker = NMFMarker()
    
    func makeUIView(context: Context) -> NMFNaverMapView {
        let view = NMFNaverMapView()
        view.showCompass = true
        view.showLocationButton = true
        view.showZoomControls = true
        view.mapView.positionMode = .direction
        view.mapView.zoomLevel = 17
        view.mapView.isIndoorMapEnabled = true
        
        return view
    }
    
    func updateUIView(_ uiView: NMFNaverMapView, context: Context) {
        let overlay = NMFPath()
        var lst: [NMGLatLng] = []
                
        overlay.path = NMGLineString(points: lst)
        overlay.mapView = uiView.mapView
        
        startPoint = Point(lon: locationManager.lastLocation?.coordinate.longitude ?? 0, lat: locationManager.lastLocation?.coordinate.latitude ?? 0)
        
        var endCoor: NMGLatLng? = nil

        if let endPoint {
            endCoor = NMGLatLng(lat: endPoint.lat, lng: endPoint.lon)
            marker.iconImage = NMF_MARKER_IMAGE_RED
        }
        
        if let startPoint {
            var startCoor = NMGLatLng(lat: startPoint.lat, lng: startPoint.lon)
            let cam = NMFCameraUpdate(scrollTo: startCoor)
            uiView.mapView.moveCamera(cam)
        }
        
        if let endCoor {
            marker.position = endCoor
            marker.mapView = uiView.mapView
            let cam = NMFCameraUpdate(scrollTo: endCoor)
            uiView.mapView.moveCamera(cam)
        }
       
    }
}
