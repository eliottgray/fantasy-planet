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

    def setUp(self) -> None:
        """Create a fixture that is quite obviously """
        a = Point(x=0.0, y=0.0, z=1.0, alt=1.0)   # Top of pyramid.
        b = Point(x=1.0, y=0.0, z=-1.0, alt=0.9)
        c = Point(x=-1.0, y=1.0, z=-1.0, alt=0.8)
        d = Point(x=-1.0, y=-1.0, z=-1.0, alt=0.7)
        self.tetra = Tetrahedron(a=a, b=b, c=c, d=d)

    def test_rotate_around_x_axis_90_degrees(self):
        rotated = self.tetra.rotate_around_x_axis(90)
        expected_a = Point(x=0.5, y=0.5, z=0.5, alt=99)
        expected = Tetrahedron(a=Point(x=0.5, y=0.5, z=0.5, alt=99), b=Point(x=0.5, y=0.5, z=0.5, alt=99), c=Point(x=0.5, y=0.5, z=0.5, alt=99), d=Point(x=0.5, y=0.5, z=0.5, alt=99))