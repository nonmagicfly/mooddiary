# Диагностика: сайт не открывается по IP

Выполните на сервере по порядку:

## 1. Контейнеры запущены?

```bash
cd /root/mooddiary/deploy
docker-compose -f docker-compose.prod.yml ps
```

Все 4 сервиса (postgres, keycloak, backend, nginx) должны быть в статусе `running`.

## 2. Nginx слушает порт 80?

```bash
ss -tlnp | grep 80
# или
netstat -tlnp | grep 80
```

Должна быть строка `*:80` или `0.0.0.0:80`.

## 3. Firewall (UFW)

```bash
ufw status
```

Порт 80 должен быть `ALLOW`.

## 4. Локальный тест на сервере

```bash
curl -I http://127.0.0.1
curl -I http://localhost
```

Если отвечает — nginx работает, проблема в сети или firewall.

## 5. Firewall провайдера

У многих хостингов (Timeweb, Selectel, REG.RU и т.д.) есть **дополнительный firewall в панели управления**. Проверьте, что порт 80 открыт в панели VPS.

## 6. Логи контейнеров

```bash
docker-compose -f docker-compose.prod.yml logs nginx
docker-compose -f docker-compose.prod.yml logs backend
docker-compose -f docker-compose.prod.yml logs keycloak
```

## 7. Перезапуск

```bash
cd /root/mooddiary/deploy
docker-compose -f docker-compose.prod.yml down
docker-compose -f docker-compose.prod.yml up -d
```
