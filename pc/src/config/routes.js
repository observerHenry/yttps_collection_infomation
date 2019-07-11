import CollectionInfo from '../components/collectionInfo.vue'
import ZipCreate from '../components/zipCreate.vue'
export default [
    {
        path:"/zipCreate",
        component: ZipCreate,
        name: 'zipCreate',
        hidden: true
    },
    {
        path:"/collectionInfo",
        component: CollectionInfo,
        name: 'collectionInfo',
        hidden: true
    },
    {
        path: '*',
        redirect: '/collectionInfo',
        hidden: true
    },
]
