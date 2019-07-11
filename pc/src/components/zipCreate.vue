<template>
    <center>
        <el-button v-on:click="createZip">
            下载zip
        </el-button>
    </center>
</template>

<script>
    export default {
        name: "zipCreate"
        ,
        methods: {
            createZip() {
                $.ajax({
                    url: HOST + 'zip/create',
                    type: 'POST',
                    dataType: "json",
                    async: true,
                    success: function (res) {
                        if (res.code == 200) {
                            window.location.href = "http://hankun-tech.com/downLoad/" + res.data;
                        }
                    }, error: function (res) {
                        alert(JSON.stringify(res));
                    }
                })
            }
        }
    }
</script>

<style scoped>

</style>