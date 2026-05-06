import http from 'k6/http';
import { check, sleep } from 'k6';

const NUM_USERS = 300;
const BASE_URL = 'http://localhost:8080';
const PASSWORD = 'Test1234!';  // 영문 + 숫자 + 특수문자 (regex 통과)

export const options = {
    /*vus: 5,
    duration: '10s',*/
    stages: [
        { duration: '20s', target: 50 },   // 0 → 50
        { duration: '20s', target: 100 },  // 50 → 100
        { duration: '20s', target: 200 },  // 100 → 200
        { duration: '20s', target: 300 },  // 200 → 300
        { duration: '20s', target: 0 },    // 300 → 0 (감소)
    ],
};

export function setup() {
    console.log(`Creating ${NUM_USERS} test users...`);

    for (let i = 0; i < NUM_USERS; i++) {
        const email = `k6test_${i}@test.com`;
        const phoneNumber = `010-1234-${String(i).padStart(4, '0')}`;

        const res = http.post(
            `${BASE_URL}/api/v1/users/signUp`,
            JSON.stringify({
                email: email,
                password: PASSWORD,
                phoneNumber: phoneNumber,
            }),
            { headers: { 'Content-Type': 'application/json' } }
        );

        // 200(성공) 또는 409(이미존재) 외의 응답은 로그로 디버깅
        if (res.status !== 200 && res.status !== 409) {
            console.log(`signup ${email}: status=${res.status}, body=${res.body}`);
        }
    }

    console.log('Waiting for wallet creation...');
    sleep(5);

    console.log('Setup complete');
}

export default function () {
    const userIdx = (__VU - 1) % NUM_USERS;
    const email = `k6test_${userIdx}@test.com`;

    // 1. 로그인
    const loginRes = http.post(
        `${BASE_URL}/api/v1/users/login`,
        JSON.stringify({ email, password: PASSWORD }),
        { headers: { 'Content-Type': 'application/json' } }
    );

    check(loginRes, { 'login status 200': (r) => r.status === 200 });

    // 로그인 실패하면 디버깅 정보 남기고 끝
    if (loginRes.status !== 200) {
        console.log(`login fail ${email}: status=${loginRes.status}, body=${loginRes.body}`);
        return;
    }

    const token = loginRes.json('data.accessToken');
    if (!token) {
        console.error(`no token for ${email}: ${loginRes.body}`);
        return;
    }

    // 2. 충전
    const orderId = `K6_${__VU}_${__ITER}_${Date.now()}`;
    const paymentKey = `FAKE_${orderId}`;
    const amount = 1000;

    const confirmRes = http.post(
        `${BASE_URL}/api/v1/payments/confirm?paymentKey=${paymentKey}&orderId=${orderId}&amount=${amount}`,
        null,
        { headers: { Authorization: `Bearer ${token}` } }
    );

    check(confirmRes, {
        'confirm status 200': (r) => r.status === 200,
    });

    sleep(1);
}