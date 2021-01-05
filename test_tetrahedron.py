import unittest
from tetrahedron import Tetrahedron
from point import Point


class TetrahedronTest(unittest.TestCase):

    def test_positive_case(self):
        a = Point(x=1.0, y=1.0, z=1.0, alt=1.0)
        b = Point(x=0.9, y=0.9, z=0.9, alt=0.9)
        c = Point(x=0.8, y=0.8, z=0.8, alt=0.8)
        d = Point(x=0.7, y=0.7, z=0.7, alt=0.7)
        tetra = Tetrahedron(a=a, b=b, c=c, d=d)
        self.assertEqual(a, tetra.a)
        self.assertEqual(b, tetra.b)
        self.assertEqual(c, tetra.c)
        self.assertEqual(d, tetra.d)


class RotateTetrahedronTest(unittest.TestCase):

    def compare_points(self, expected_point, actual_point):

        # Cartesian coordinates may be approximate, as they are the result of a transformation.
        self.assertAlmostEqual(expected_point.x, actual_point.x)
        self.assertAlmostEqual(expected_point.y, actual_point.y)
        self.assertAlmostEqual(expected_point.z, actual_point.z)

        # Spherical coordinates must have been stored without being transformed.
        self.assertEqual(expected_point.lat, actual_point.lat)
        self.assertEqual(expected_point.lon, actual_point.lon)
        self.assertEqual(expected_point.alt, actual_point.alt)

    def compare_tetrahedrons(self, expected_tetrahedron, actual_tetrahedron):
        self.compare_points(expected_tetrahedron.a, actual_tetrahedron.a)
        self.compare_points(expected_tetrahedron.b, actual_tetrahedron.b)
        self.compare_points(expected_tetrahedron.c, actual_tetrahedron.c)
        self.compare_points(expected_tetrahedron.d, actual_tetrahedron.d)

    def setUp(self) -> None:
        """Create a fixture that is quite obviously """
        a = Point(x=0.0, y=0.0, z=1.0, alt=1.0)   # Top of pyramid.
        b = Point(x=1.0, y=0.0, z=-1.0, alt=0.9)
        c = Point(x=-1.0, y=1.0, z=-1.0, alt=0.8)
        d = Point(x=-1.0, y=-1.0, z=-1.0, alt=0.7)
        self.tetra = Tetrahedron(a=a, b=b, c=c, d=d)

    def test_rotate_around_x_axis_90_degrees(self):
        a = Point(x=0.0, y=-1.0, z=0.0, alt=1.0)
        b = Point(x=1.0, y=1.0, z=0.0, alt=0.9)
        c = Point(x=-1.0, y=1.0, z=1.0, alt=0.8)
        d = Point(x=-1.0, y=1.0, z=-1.0, alt=0.7)
        expected = Tetrahedron(a=a, b=b, c=c, d=d)
        actual = self.tetra.rotate_around_x_axis(90)
        self.compare_tetrahedrons(expected_tetrahedron=expected, actual_tetrahedron=actual)

