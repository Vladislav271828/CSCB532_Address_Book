import axios from 'axios';

export default axios.create({
    baseURL: 'http://192.168.1.2:8080/api/v1'
});