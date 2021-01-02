import unittest
from point import Point


class PointFromSphericalCoordinatesTest(unittest.TestCase):

    def run_test(self, lat, lon, x, y, z):
        actual_point = Point.from_spherical(lat=lat, lon=lon)
        self.assertAlmostEqual(x, actual_point.get_x())
        self.assertAlmostEqual(y, actual_point.get_y())
        self.assertAlmostEqual(z, actual_point.get_z())
        self.assertAlmostEqual(lat, actual_point.get_lat())
        self.assertAlmostEqual(lon, actual_point.get_lon())
        print(actual_point)

    def test_origin(self):
        self.run_test(lat=0, lon=0, x=1, y=0, z=0)

    def test_poles(self):
        self.run_test(lat=90.0, lon=0.0, x=0, y=0, z=1)
        self.run_test(lat=-90.0, lon=90.0, x=0, y=0, z=-1)

    def test_180_longitude(self):
        self.run_test(lat=0.0, lon=180, x=-1, y=0, z=0)
        self.run_test(lat=0.0, lon=-180, x=-1, y=0, z=0)
