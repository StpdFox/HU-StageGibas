class WarehouseLocation:
    """
    Abstract datatype representing locations within the Effimat

    """

    def __init__(self, row=0, column=0, side="X"):
        self.row = row  # Contains the row number
        self.column = column  # Contains the column number
        self.side = side  # Contains the side, either F (front) or B (back)

    def set_all(self, all_info):
        """
        Function that sets all variables to the info provided (in a list)

        :param all_info: A list containing the information of a location
        :return:
        """
        self.row = all_info.split(",")[0]  # 1st element: Row
        self.column = all_info.split(",")[1]  # 2nd element: Column
        self.side = all_info.split(",")[2]  # 3th element: Side
