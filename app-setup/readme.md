
1. cấp quyền, trong thư mục chứa docker-compose.yml, chạy lệnh:

chmod +x odoo-init.sh

2. chạy

docker compose up -d 

3. tắt
3.1 clear 
docker compose -p traloitudongV2 down -v --rmi all --remove-orphans
docker compose down -v

===================================================================

docker exec -it traloitudong_postgres psql -U postgres -d postgres

\c odoo_db;