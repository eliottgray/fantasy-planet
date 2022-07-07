<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <!-- Include the CesiumJS JavaScript and CSS files -->
 <link rel="stylesheet" href="https://unpkg.com/leaflet@1.8.0/dist/leaflet.css"
   integrity="sha512-hoalWLoI8r4UszCkZ5kL8vayOGVae1oxXe/2A4AO6J9+580uKHDO3JdHb7NzwwzK5xr/Fs0W40kiNHxM9vyTtQ=="
   crossorigin=""/>
    <!-- Make sure you put this AFTER Leaflet's CSS -->
    <script src="https://unpkg.com/leaflet@1.8.0/dist/leaflet.js"
      integrity="sha512-BB3hKbKWOc9Ez/TAwyWxNXeoV9c1v6FIeYiBieIWkpLjauysF18NzgR1MBNBXf8/KABdlkX68nAhlwcDFLGPCQ=="
      crossorigin=""></script>

      <style>
      #map { height: 720px; width: 1280px; }
      </style>
</head>
<body>
  <div id="map"></div>
  <script>

    var seed = ${seed};
    var maxDepth = ${depth};

    var map = L.map('map').setView([0.0, 0.0], 2);
    L.tileLayer('https://htxlsr5vv8.execute-api.us-west-1.amazonaws.com/tiles/12345/{z}/{x}/{y}', {
        maxZoom: 19,
        attribution: '© OpenStreetMap'
    }).addTo(map);

  </script>
</body>
</html>