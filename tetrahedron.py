from point import Point


class Tetrahedron:

    def __init__(self, a: Point, b: Point, c: Point, d: Point):
        self.a = a
        self.b = b
        self.c = c
        self.d = d

    def rotate_around_x_axis(self, degrees: float):
        new_a = self.a.rotate_around_x_axis(degrees)
        new_b = self.b.rotate_around_x_axis(degrees)
        new_c = self.c.rotate_around_x_axis(degrees)
        new_d = self.d.rotate_around_x_axis(degrees)
        return Tetrahedron(a=new_a, b=new_b, c=new_c, d=new_d)

    def rotate_around_y_axis(self, degrees: float):
        new_a = self.a.rotate_around_y_axis(degrees)
        new_b = self.b.rotate_around_y_axis(degrees)
        new_c = self.c.rotate_around_y_axis(degrees)
        new_d = self.d.rotate_around_y_axis(degrees)
        return Tetrahedron(a=new_a, b=new_b, c=new_c, d=new_d)

    def contains(self, point: Point):
        # See "An efficient point in polyhedron algorithm" by Jeff Lane, Bob Magedson, Mike Rarick:
        #  https://doi.org/10.1016/0734-189X(84)90133-6
        if self.is_vertex(point):
            return True
        raise NotImplementedError

    def is_vertex(self, point: Point):
        if point == self.a or point == self.b or point == self.c or point == self.d:
            return True
        else:
            return False