from planet import Planet
import h3
import json


class Writer(object):

    def __init__(self, h3_depth: int):
        self.depth = h3_depth
        edge_length = h3.edge_length(h3_depth, unit='m')
        print("edge length", edge_length)
        self.planet = Planet(resolution=edge_length * 0.6)

    def make_geojson(self, h3_node) -> dict:
        lat, lng = h3.h3_to_geo(h3_node)
        # polygon = h3.h3_to_geo_boundary(h3_node, geo_json=False)
        feature = {
            "type": 'Feature',
            "properties": {
                "h3": h3_node,
                "alt": self.planet.get_elevation_at(lat=lat, lon=lng)
            },
            "geometry": {
                "type": 'Point',
                "coordinates":  [lng, lat]
            }
        }
        return feature


    def recurse_write(self, current, depth, f):
        if depth >= self.depth:
            geojson = self.make_geojson(current)
            geojson_str = json.dumps(geojson)
            f.write(geojson_str)
            f.write("\n")
        else:
            children = h3.h3_to_children(current)
            for child in children:
                self.recurse_write(child, depth + 1, f)

    def write(self, filepath: str):
        res0 = h3.get_res0_indexes()

        with open(filepath, "w") as outputf:
            for top_cell in res0:
                self.recurse_write(top_cell, 0, outputf)


writer = Writer(h3_depth=2)
writer.write("test_geojson_out.json")