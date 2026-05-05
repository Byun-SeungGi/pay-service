import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    stages: [
        { duration: '20s', target: 50 },   // 0 → 50명까지 20초간 증가
        { duration: '20s', target: 100 },  // 50 → 100명
        { duration: '20s', target: 200 },  // 100 → 200명
        { duration: '20s', target: 300 },  // 200 → 300명
        { duration: '20s', target: 0 },    // 천천히 종료
    ],
};

const BASE_URL = 'http://localhost:8080';
const EMAIL = 'bsg@naver.com';
const PASSWORD = 'dkflfkd12!@';

export default function () {
    // 1. 로그인
    const loginRes = http.post(
        `${BASE_URL}/api/v1/users/login`,
        JSON.stringify({ email: EMAIL, password: PASSWORD }),
        { headers: { 'Content-Type': 'application/json' } }
    );

    check(loginRes, {
        'login status 200': (r) => r.status === 200,
    });

    // 토큰 추출
    const token = loginRes.json('data.accessToken');

    if (!token) {
        console.error('login failed:', loginRes.body);
        return;
    }

    // 2. 결제 조회 (JWT 인증 헤더 포함)
    const findRes = http.get(`${BASE_URL}/api/v1/payments/find`, {
        headers: { Authorization: `Bearer ${token}` },
    });

    check(findRes, {
        'find status 200': (r) => r.status === 200,
    });

    sleep(1);
}