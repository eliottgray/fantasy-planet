<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <!-- Include the CesiumJS JavaScript and CSS files -->
  <script src="https://cesium.com/downloads/cesiumjs/releases/1.85/Build/Cesium/Cesium.js"></script>
  <link href="https://cesium.com/downloads/cesiumjs/releases/1.85/Build/Cesium/Widgets/widgets.css" rel="stylesheet">
</head>
<body>
  <div id="cesiumContainer"></div>
  <script>

    var seed = ${seed};
    var maxDepth = ${depth};

    const viewer = new Cesium.Viewer('cesiumContainer', {
        // Base layers include helpfully pre-populated, but unnecessary for our use case, real world data.
        // Some of the available layers additionally require a Cesium Ion API key, and trigger a nag.
        baseLayerPicker : false,

        // Geocoder relates to real world data, and also triggers nag regarding api key.
        geocoder: false,

        // TODO: enable a default spinning animation.
        animation: false,

        imageryProvider: new Cesium.UrlTemplateImageryProvider({
          url : '/tiles/{seed}/{z}/{x}/{y}.png',
          maximumLevel: maxDepth,
          tilingScheme : new Cesium.GeographicTilingScheme(),
          customTags : {
            seed: function(imageryProvider, x, y, level) {
              return seed
            }
          }
        })
    });

  </script>
</body>
</html>