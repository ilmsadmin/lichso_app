// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "LichSo",
    platforms: [
        .iOS(.v17)
    ],
    products: [
        .library(
            name: "LichSo",
            targets: ["LichSo"]
        ),
    ],
    targets: [
        .target(
            name: "LichSo",
            path: "LichSo"
        ),
    ]
)
