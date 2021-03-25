from numbers import Number


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
