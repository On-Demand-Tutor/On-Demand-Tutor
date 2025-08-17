import pymysql
from pymysql.cursors import DictCursor

try:
    connection = pymysql.connect(
        host='localhost',
        user='root',
        password='Cphu1011005@',
        database='adminmoderatordb',
        port=3306,
        cursorclass=DictCursor,
        connect_timeout=10,
        ssl={'fake_flag_to_enable_cryptography': True}  
    )

    cursor = connection.cursor()
    cursor.execute("SELECT USER();")
    user_result = cursor.fetchone()
    print("Kết nối thành công! User hiện tại:", user_result)

    cursor.execute("SHOW DATABASES;")
    databases = cursor.fetchall()
    print("Danh sách databases:", databases)

except pymysql.Error as e:
    print(f"Lỗi kết nối database: {str(e)}")
    print(f"Chi tiết lỗi: Code {e.args[0]}, Message: {e.args[1]}")

finally:
    if 'connection' in locals():
        cursor.close()
        connection.close()
        print("Đã đóng kết nối.")