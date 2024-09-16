import { createPinia } from "pinia";
import videojs from "video.js";
import "video.js/dist/video-js.css";
import { createApp } from 'vue';
import App from './App.vue';
import vuetify from './plugins/vuetify';
import router from './router';

// 创建应用
const luckJourneyApp = createApp(App)
luckJourneyApp.use(createPinia())
luckJourneyApp.use(router)
luckJourneyApp.use(vuetify)
luckJourneyApp.config.globalProperties.$video = videojs;
luckJourneyApp.mount('#app')

