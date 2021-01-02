import unittest
from point import Point


class PointFromSphericalCoordinatesTest(unittest.TestCase):

    def run_test(self, lat=0.0, lon=0.0, alt=0.0, x=0, y=0, z=0):
        actual_point = Point.from_spherical(lat=lat, lon=lon, alt=alt)

        # Cartesian coordinates may be approximate.
        self.assertAlmostEqual(x, actual_point.get_x())
        self.assertAlmostEqual(y, actual_point.get_y())
        self.assertAlmostEqual(z, actual_point.get_z())

        # Spherical coordinates must not have been transformed.
        self.assertEqual(lat, actual_point.get_lat())
        self.assertEqual(lon, actual_point.get_lon())
        self.assertEqual(alt, actual_point.get_alt())

    def test_x_axis(self):
        """The Origin (0.0, 0.0) and it's Antipode (Longitude of 180 or -180) represent the bounds of the X axis."""
        self.run_test(lat=0, lon=0, x=1, y=0, z=0)
        self.run_test(lat=0, lon=180, x=-1, y=0, z=0)
        self.run_test(lat=0, lon=-180, x=-1, y=0, z=0)

    def test_poles(self):
        """The poles represent the bounds of the Z axis."""
        self.run_test(lat=90.0, lon=0.0, x=0, y=0, z=1)
        self.run_test(lat=-90.0, lon=0.0, x=0, y=0, z=-1)

    def test_90_longitude(self):
        """90 and negative 90 represent the bounds of the Y axis."""
        self.run_test(lat=0.0, lon=180, x=-1, y=0, z=0)
        self.run_test(lat=0.0, lon=-180, x=-1, y=0, z=0)
