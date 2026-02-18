'use strict';

// ─── Configuratie ─────────────────────────────────────────────────────────────
const API_KEY     = 'b2993d09a467a0172653bfecc4851b74';
const FORECAST    = 'https://api.openweathermap.org/data/2.5/forecast';
const UVI        = 'https://api.openweathermap.org/data/2.5/uvi';

// ─── Fietsen ──────────────────────────────────────────────────────────────────
const BIKES = [
    {
        name: 'Ultimate',
        subtitle: 'Race fiets',
        subtype: '',
        image: 'images/bike_ultimate.jpg',
        gradient: 'linear-gradient(145deg, #0D0D0D 0%, #2C2020 100%)',
    },
    {
        name: 'Atalaya',
        subtitle: 'Gravel fiets',
        subtype: '',
        image: 'images/bike_atalaya.jpg',
        gradient: 'linear-gradient(145deg, #C8C8C8 0%, #F0F0F0 100%)',
    },
    {
        name: 'Neuron',
        subtitle: 'Mountain bike',
        subtype: 'Fully',
        image: 'images/bike_neuron.jpg',
        gradient: 'linear-gradient(145deg, #E8E0D0 0%, #F8F4EE 100%)',
    },
    {
        name: 'Exceed',
        subtitle: 'Mountain bike',
        subtype: 'Hardtail',
        image: 'images/bike_exceed.jpg',
        gradient: 'linear-gradient(145deg, #0A0F1A 0%, #1C2B3A 100%)',
    },
];

// ─── Checklijsten (exact zoals originele app) ─────────────────────────────────
const CHECKLISTS = {
    Ultimate: [
        { id: 'u1',  text: 'Raceschoenen' },
        { id: 'u2',  text: 'Twee bidons' },
        { id: 'u3',  text: 'Reparatietas en bandje' },
        { id: 'u4',  text: 'Wahoo groot' },
        { id: 'u5',  text: 'Handschoenen' },
        { id: 'u6',  text: 'Bril' },
        { id: 'u7',  text: 'Muts' },
        { id: 'u8',  text: 'Overschoenen' },
        { id: 'u9',  text: 'Eten' },
        { id: 'u10', text: 'Verlichting' },
    ],
    Atalaya: [
        { id: 'a1',  text: 'Klikschoen of Flatschoen' },
        { id: 'a2',  text: 'Twee bidons' },
        { id: 'a3',  text: 'Reparatiebidon' },
        { id: 'a4',  text: 'Batterij' },
        { id: 'a5',  text: 'Wahoo groot' },
        { id: 'a6',  text: 'Handschoenen' },
        { id: 'a7',  text: 'Bril' },
        { id: 'a8',  text: 'Muts' },
        { id: 'a9',  text: 'Overschoenen' },
        { id: 'a10', text: 'Eten' },
        { id: 'a11', text: 'Verlichting' },
    ],
    Neuron: [
        { id: 'n1',  text: 'Klikschoen of Flatschoen' },
        { id: 'n2',  text: 'Één bidon of drinkzak' },
        { id: 'n3',  text: 'Reparatietas' },
        { id: 'n4',  text: 'Batterij' },
        { id: 'n5',  text: 'Wahoo groot' },
        { id: 'n6',  text: 'Handschoenen' },
        { id: 'n7',  text: 'Bril' },
        { id: 'n8',  text: 'Muts' },
        { id: 'n9',  text: 'Overschoenen' },
        { id: 'n10', text: 'Eten' },
        { id: 'n11', text: 'Verlichting' },
    ],
    Exceed: [
        { id: 'e1',  text: 'Klikschoenen' },
        { id: 'e2',  text: 'Twee bidons' },
        { id: 'e3',  text: 'Reparatietas' },
        { id: 'e4',  text: 'Wahoo klein' },
        { id: 'e5',  text: 'Handschoenen' },
        { id: 'e6',  text: 'Bril' },
        { id: 'e7',  text: 'Muts' },
        { id: 'e8',  text: 'Overschoenen' },
        { id: 'e9',  text: 'Eten' },
        { id: 'e10', text: 'Verlichting' },
    ],
};

// ─── Weer-conditionele items ───────────────────────────────────────────────────
function conditionalItems(tempC, uvi) {
    const items = [];
    if (uvi >= 3) {
        items.push({ id: 'weather_sunscreen', text: 'Zonnebrand',        tag: '☀️ UV',   tagClass: 'item-tag--uv' });
        items.push({ id: 'weather_lipbalm',   text: 'Lipbalsem',         tag: '☀️ UV',   tagClass: 'item-tag--uv' });
    }
    if (tempC < 5) {
        items.push({ id: 'weather_hat',       text: 'Muts',              tag: '🥶 Koud', tagClass: 'item-tag--cold' });
        items.push({ id: 'weather_gloves',    text: 'Warme handschoenen',tag: '🥶 Koud', tagClass: 'item-tag--cold' });
        items.push({ id: 'weather_overshoes', text: 'Overschoenen',      tag: '🥶 Koud', tagClass: 'item-tag--cold' });
    }
    return items;
}

// ─── State ────────────────────────────────────────────────────────────────────
let currentBike   = null;
let weatherTemp   = null;
let weatherUvi    = 0;
let weatherTxt    = 'Weer ophalen…';

// ─── Opslag ───────────────────────────────────────────────────────────────────
function saveState(bikeName, items) {
    const state = {};
    items.forEach(it => { state[it.id] = it.checked; });
    localStorage.setItem('bc_' + bikeName, JSON.stringify(state));
}

function loadState(bikeName) {
    try { return JSON.parse(localStorage.getItem('bc_' + bikeName)) || {}; }
    catch { return {}; }
}

// ─── Navigatie ────────────────────────────────────────────────────────────────
function showMain() {
    document.getElementById('checklist-screen').classList.add('hidden');
    document.getElementById('main-screen').classList.remove('hidden');
    currentBike = null;
}

function showChecklist(bike) {
    currentBike = bike;
    document.getElementById('main-screen').classList.add('hidden');
    document.getElementById('checklist-screen').classList.remove('hidden');
    document.getElementById('checklist-title').textContent = bike.name;
    document.getElementById('weather-text-checklist').textContent = weatherTxt;
    renderChecklist();
}

// ─── Fietskaarten renderen ────────────────────────────────────────────────────
function renderBikeGrid() {
    const grid = document.getElementById('bike-grid');
    grid.innerHTML = BIKES.map(bike => `
        <div class="bike-card" data-bike="${bike.name}">
            <div class="bike-photo" style="
                background-image: url('${bike.image}'), ${bike.gradient};
                background-size: cover;
                background-position: center;
            "></div>
            <div class="bike-info">
                <div class="bike-name">${bike.name}</div>
                <div class="bike-row">
                    <span class="bike-subtitle">${bike.subtitle}</span>
                    ${bike.subtype ? `<span class="bike-subtype">${bike.subtype}</span>` : ''}
                </div>
            </div>
        </div>
    `).join('');

    grid.querySelectorAll('.bike-card').forEach(card => {
        card.addEventListener('click', () => {
            const bike = BIKES.find(b => b.name === card.dataset.bike);
            if (bike) showChecklist(bike);
        });
    });
}

// ─── Checklist renderen ───────────────────────────────────────────────────────
function renderChecklist() {
    if (!currentBike) return;

    const saved      = loadState(currentBike.name);
    const baseItems  = CHECKLISTS[currentBike.name].map(it => ({
        ...it, checked: saved[it.id] ?? false, tag: '', tagClass: ''
    }));

    // Weer-items (bewaarde staat of standaard false)
    const condItems  = conditionalItems(weatherTemp ?? 15, weatherUvi).map(it => ({
        ...it, checked: saved[it.id] ?? false
    }));

    const allItems = [...baseItems, ...condItems];

    const list = document.getElementById('checklist-items');
    list.innerHTML = allItems.map(it => `
        <li class="checklist-item${it.checked ? ' checked' : ''}" id="li-${it.id}">
            <label>
                <input type="checkbox" data-id="${it.id}" ${it.checked ? 'checked' : ''}>
                <span class="item-content">
                    <span class="item-text">${it.text}</span>
                    ${it.tag ? `<span class="item-tag ${it.tagClass}">${it.tag}</span>` : ''}
                </span>
            </label>
        </li>
    `).join('');

    // Checkbox events
    list.querySelectorAll('input[type="checkbox"]').forEach(cb => {
        cb.addEventListener('change', () => {
            const li = document.getElementById('li-' + cb.dataset.id);
            li.classList.toggle('checked', cb.checked);
            const item = allItems.find(it => it.id === cb.dataset.id);
            if (item) item.checked = cb.checked;
            saveState(currentBike.name, allItems);
            updateProgress(allItems);
        });
    });

    updateProgress(allItems);
}

function updateProgress(items) {
    const total   = items.length;
    const checked = items.filter(it => it.checked).length;
    const pct     = total > 0 ? Math.round(checked / total * 100) : 0;
    document.getElementById('progress-fill').style.width  = pct + '%';
    document.getElementById('progress-count').textContent = `${checked} / ${total}`;
    document.getElementById('progress-pct').textContent   = pct + '%';
}

// ─── Weer ophalen ─────────────────────────────────────────────────────────────
async function fetchWeather(lat, lon) {
    try {
        const [fRes, uviRes] = await Promise.allSettled([
            fetch(`${FORECAST}?lat=${lat}&lon=${lon}&units=metric&appid=${API_KEY}`).then(r => r.json()),
            fetch(`${UVI}?lat=${lat}&lon=${lon}&appid=${API_KEY}`).then(r => r.json()),
        ]);

        if (fRes.status !== 'fulfilled') throw new Error('Forecast mislukt');
        const f = fRes.value;

        const current    = f.list[0];
        const in4h       = f.list[1];
        const temp       = Math.round(current.main.temp);
        const temp4h     = Math.round(in4h.main.temp);
        const rain       = Math.round(current.pop * 100);
        const wid        = current.weather[0].id;
        weatherTemp      = current.main.temp;

        const emoji = wid >= 200 && wid < 300 ? '⛈️'
                    : wid >= 300 && wid < 600 ? '🌧️'
                    : wid >= 600 && wid < 700 ? '🌨️'
                    : wid >= 700 && wid < 800 ? '🌫️'
                    : wid === 800             ? '☀️'
                    : '☁️';

        weatherTxt = `${emoji} ${temp}°C    🕐 Over 4 uur: ${temp4h}°C`;
        if (rain > 30) weatherTxt += `\n⚠️ ${rain}% kans op regen`;

        // UV-index
        if (uviRes.status === 'fulfilled' && uviRes.value?.value != null) {
            weatherUvi = uviRes.value.value;
            if (weatherUvi >= 3) {
                const level = weatherUvi >= 8 ? 'zeer hoog' : weatherUvi >= 6 ? 'hoog' : 'matig';
                weatherTxt += `\n🔆 UV-index ${Math.round(weatherUvi)} (${level})`;
            }
        }
    } catch {
        weatherTxt = 'Kon het weer niet ophalen';
    }

    // Beide weerkaarten bijwerken
    document.getElementById('weather-text-main').textContent      = weatherTxt;
    document.getElementById('weather-text-checklist').textContent = weatherTxt;

    // Conditionele items verversen als checklist open is
    if (currentBike) renderChecklist();
}

function initWeather() {
    if (!navigator.geolocation) {
        weatherTxt = 'Locatie niet beschikbaar';
        document.getElementById('weather-text-main').textContent = weatherTxt;
        return;
    }
    navigator.geolocation.getCurrentPosition(
        pos => fetchWeather(pos.coords.latitude, pos.coords.longitude),
        ()  => {
            weatherTxt = 'Locatie geweigerd';
            document.getElementById('weather-text-main').textContent = weatherTxt;
        }
    );
}

// ─── Reset-dialoog ────────────────────────────────────────────────────────────
function setupResetDialog() {
    // Voeg dialog toe aan DOM
    const overlay = document.createElement('div');
    overlay.className = 'overlay hidden';
    overlay.id = 'reset-overlay';
    overlay.innerHTML = `
        <div class="dialog">
            <h2>Checklist resetten</h2>
            <p>Alle vinkjes verwijderen voor deze fiets?</p>
            <div class="dialog-btns">
                <button class="btn btn--cancel" id="cancel-reset">Annuleren</button>
                <button class="btn btn--confirm" id="confirm-reset">Reset</button>
            </div>
        </div>
    `;
    document.body.appendChild(overlay);

    document.getElementById('reset-btn').addEventListener('click', () => {
        overlay.classList.remove('hidden');
    });
    document.getElementById('cancel-reset').addEventListener('click', () => {
        overlay.classList.add('hidden');
    });
    document.getElementById('confirm-reset').addEventListener('click', () => {
        overlay.classList.add('hidden');
        if (currentBike) {
            localStorage.removeItem('bc_' + currentBike.name);
            renderChecklist();
        }
    });
    overlay.addEventListener('click', e => {
        if (e.target === overlay) overlay.classList.add('hidden');
    });
}

// ─── Service Worker registreren (offline) ────────────────────────────────────
function registerSW() {
    if ('serviceWorker' in navigator) {
        navigator.serviceWorker.register('sw.js').catch(() => {});
    }
}

// ─── Start ────────────────────────────────────────────────────────────────────
document.addEventListener('DOMContentLoaded', () => {
    renderBikeGrid();
    setupResetDialog();
    registerSW();
    initWeather();

    document.getElementById('back-btn').addEventListener('click', showMain);

    // Elke 30 minuten weer verversen
    setInterval(initWeather, 30 * 60 * 1000);
});
