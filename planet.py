from numbers import Number
from tetrahedron import Tetrahedron
from point import Point


class PlanetError(Exception):
    pass


class Planet(object):

    def __init__(self, resolution=10):
        """
        :param resolution: Approximate resolution of the calculated planet.
        """
        if not isinstance(resolution, Number):
            raise PlanetError("Provided resolution parameter must be a Number.")
        self.resolution = resolution
        self.tetra = Tetrahedron.build_default()

    def get_elevation_at(self, lat: float, lon: float):
        point = Point(lat=lat, lon=lon)
        current = self.tetra
        while current.get_longest_side_length() > self.resolution:
            sub_one, sub_two = current.subdivide()
            if sub_one.contains(point):
                current = sub_one
            else:
                assert sub_two.contains(point)
                current = sub_two
        elevation = current.average_altitude()
        return elevation
