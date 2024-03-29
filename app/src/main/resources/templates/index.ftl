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

        // Since our data is static, no timeline is required.
        timeline: false,

        sceneMode: Cesium.SceneMode.SCENE3D,

        imageryProvider: new Cesium.UrlTemplateImageryProvider({
          url : '/tiles/{seed}/{z}/{x}/{y}.png',
          maximumLevel: maxDepth,
          tilingScheme : new Cesium.GeographicTilingScheme({numberOfLevelZeroTilesX: 1}),
          customTags : {
            seed: function(imageryProvider, x, y, level) {
              return seed
            }
          }
        })
    });

    // TODO: Is there a better way to set the default location than a fly-to with duration zero?  Set default, instead?
    viewer.camera.flyTo({
        duration: 0,
        destination: Cesium.Cartesian3.fromDegrees(0.0, 0.0, 25000000.0)
      });

    function addLabel(planetName) {
      const entity = viewer.entities.add({
        position: Cesium.Cartesian3.fromDegrees(
          0.0,
          90.0,
          1500000.0
        ),
        label: {
          text: planetName,
        },
      });

      entity.label.scale = 1.5;
      entity.label.showBackground = false;
    }

    function addPlanetName() {
        fetch("https://pamelaschainfunction-apim.azure-api.net/PamelasChainFunction/ChainFunction?kind=planet&seed=" + parseInt(seed * 1000000), {
          headers: {
            "Ocp-Apim-Subscription-Key": "${nameGeneratorKey}"
          }
        }).then((response) => {
                if (response.ok) return response.text();
                else throw new Error("Name generator fetch threw status code: " + response.status)
        })
        .then(addLabel)
        .catch(console.error)
    }

    addPlanetName()

  </script>
</body>
</html>