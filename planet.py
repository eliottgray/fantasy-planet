from numbers import Number
from tetrahedron import Tetrahedron
from point import Point
from defaults import DEFAULT_SEED


class PlanetError(Exception):
    pass


class Planet(object):

    def __init__(self, seed=DEFAULT_SEED, resolution=10):
        """
        :param resolution: Approximate resolution of the calculated planet.
        """
        if not isinstance(resolution, Number):
            raise PlanetError("Provided resolution parameter must be a Number.")
        self.resolution = resolution
        self.tetra = Tetrahedron.build_default(seed)

    def get_elevation_at(self, lat: float, lon: float):
        point = Point.from_spherical(lat=lat, lon=lon)
        current = self.tetra
        subdivisions = 0
        while current.get_longest_side_length() > self.resolution:
            subdivisions += 1
            # print(subdivisions, current.get_longest_side_length())
            sub_one, sub_two = current.subdivide()
            if sub_one.contains(point):
                current = sub_one
            else:
                assert sub_two.contains(point)  # TODO: only during testing, or not at all.
                current = sub_two
        elevation = current.average_altitude()
        return elevation
