class Order:
    """
    ADT representing the parameters needed to execute an order
    """
    def __init__(self, order_type, carrier_id, location):
        """
        Initialiser for an Order object

        :param order_type: String representing if it's a store or retrieve order
        :param carrier_id: Integer representing the carrier ID
        :param location: String representing where the carrier needs to go, or needs to come from
        """
        self.order_type = order_type
        self.carrier_id = carrier_id
        self.location = location
