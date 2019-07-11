<template>
    <div style="display: block;text-align: center">
        <section id="title-section">
            <div id="page-title">
                <h2 style="color: #fff;margin-top: 0;">人脸平台信息采集</h2>
            </div>
        </section>
        <h3 style="margin-top: 30px;text-align: center;font-size: 0.9rem;color: #114B9A;line-height: 1.4rem;">
            访客您好，请填写您的基本信息</h3>
        <div class="invitation-info">
            <el-row class="input-info">
                <el-col :span="4">姓名</el-col>
                <el-col :span="16" :offset="2"><input type="text" placeholder="输入您的姓名" v-model="staffName"
                                                      v-on:change="checkName()" id="visit-emp">
                </el-col>
            </el-row>
            <el-row class="input-info">
                <el-col :span="4">手机号</el-col>
                <el-col :span="16" :offset="2"><input type="text" placeholder="请输入您的手机号码" v-model="staffPhone"
                                                      v-on:change="checkPhone()" id="visit-emp-phone">
                </el-col>
            </el-row>
            <el-row class="input-info">
                <el-col :span="4">工号</el-col>
                <el-col :span="16" :offset="2"><input type="text" placeholder="请输入您的工号" v-model="staffId"
                                                      v-on:change="checkPhone()" id="visit-emp-staffId">
                </el-col>
            </el-row>
            <el-row class="input-info">
                <el-col :span="4">标签</el-col>
                <el-col :span="16" :offset="2">
                    <el-select
                            v-model="tag.tagName"
                            multiple
                            filterable
                            remote
                            reserve-keyword
                            placeholder="请输入关键词"
                            :remote-method="remoteMethod"
                            style="outline: 0;width: 100%"
                            :loading="loadingTag"
                            :change="checkButtonIsValid">
                        <el-option
                                style="border: 0;outline: none;"
                                v-for="item in tag.tagShowList"
                                :key="item"
                                :label="item"
                                :value="item">
                        </el-option>
                    </el-select>
                    <!--<input type="text" placeholder="请输入您的工号" v-model="staffId"
                           v-on:blur="checkPhone()" id="visit-emp-tag">-->
                </el-col>
            </el-row>
            <el-row>
                <el-col :span="14">图片上传</el-col>
            </el-row>
            <div>
                <el-row
                        style="margin: 0 0 5%;padding: 0;border: 0.1rem solid #F0F0F0;position: relative">
                    <input type="file" accept="image/*" capture="camera" mutiple="mutiple"
                           v-on:change="upload()"
                           style="display: none;"
                           class="imgcamera">

                    <input type="file" accept="image/*" value="相册" style="display: none" class="imgchose"
                           v-on:change="upload()">
                    <el-col :span="24">
                        <div class="img" v-on:click="setAvatar()">
                            <img :src="faceBase64" v-if="loading==false" id="imgBase64"/>
                            <span v-if="faceBase64==''" style="font-size: 0.9rem;width: 4rem;display: inline-block">点击添加人脸图片</span>
                            <div v-if="loading" class="showimg">
                                <mt-spinner type="double-bounce" color="#26a2ff"></mt-spinner>
                            </div>
                            <mt-actionsheet
                                    :actions="actions"
                                    v-model="sheetVisible">
                            </mt-actionsheet>
                        </div>
                    </el-col>
                </el-row>
            </div>
        </div>

        <section id="button-section">
            <div></div>
            <div style="margin-top:5%;">
                <button id="add-appointment" disabled="disabled"
                        style="background-color: rgb(228, 230, 233); color: rgb(181, 181, 181)"
                        v-on:click="invitation">确认提交
                </button>
            </div>
        </section>


    </div>
</template>

<script>
    let _this;
    import {MessageBox} from 'mint-ui';
    import {Toast} from 'mint-ui';
    import {Popup} from 'mint-ui';
    import {Picker} from 'mint-ui';
    import {DatetimePicker} from 'mint-ui'
    import {EXIF} from '../assets/js/exif/exif.min.js';
    import trackings from '../assets/js/tracking-min.js'
    import tagArray from '../assets/js/tag/tagArray.js'
    import '../assets/js/face-min.js'

    Date.prototype.Format = function (fmt) {
        var o = {
            "M+": this.getMonth() + 1, //月份
            "d+": this.getDate(), //日
            "h+": this.getHours(), //小时
            "m+": this.getMinutes(), //分
            "s+": this.getSeconds(), //秒
            "q+": Math.floor((this.getMonth() + 3) / 3), //季度
            "S": this.getMilliseconds() //毫秒
        };
        if (/(y+)/.test(fmt))
            fmt = fmt.replace(RegExp.$1, (this.getFullYear() + "").substr(4 - RegExp.$1.length));
        for (var k in o) {
            if (new RegExp("(" + k + ")").test(fmt)) {
                fmt = fmt.replace(RegExp.$1, (RegExp.$1.length == 1) ? (o[k]) : (("00" + o[k]).substr(("" + o[k]).length)));
            }
        }
        return fmt;
    }

    export default {
        name: "collectionInfo",
        data() {
            _this = this;
            return {
                imgChose: "",
                actions: [{
                    name: '拍照',
                    method: this.getCamera	// 调用methods中的函数
                }, {
                    name: '从相册中选择',
                    method: this.getLibrary	// 调用methods中的函数
                }],
                sheetVisible: false,
                startTime: "",
                endTime: "",
                group: "0",
                staffId: "",
                staffPhone: "",
                staffName: "",
                loadingTag: false,
                visitorNameMoment: "",
                loading: false,
                choseChange: 0,
                visitPurpose: "0",
                faceBase64: "",
                tabIndex: 0,
                tag: {
                    tagName: [],
                    tagShowList: [],
                    tagAll: [],
                }
            }
        }, methods: {
            remoteMethod(query) {
                if (query !== '') {
                    _this.loadingTag = true;
                    setTimeout(() => {
                        this.loadingTag = false;
                        _this.tag.tagShowList = _this.tag.tagAll.filter(item => {
                            return item.toLowerCase()
                                .indexOf(query.toLowerCase()) > -1;
                        });
                    }, 200);
                } else {
                    _this.tag.tagName = _this.tag.tagAll;
                }
            },
            invitation() {
                var tag="";
                for (var i=0;i<_this.tag.tagName.length;i++){
                    tag+=_this.tag.tagName[i]
                    if (i<_this.tag.tagName.length-1){
                        tag+="/"
                    }
                }
                var info=JSON.stringify({staffId: _this.staffId,
                    base64Face: _this.faceBase64,
                    phone: _this.staffPhone,
                    tagName:tag,
                    name: _this.staffName})
                $.ajax({
                    url: HOST + 'staff/setStaff',
                    type: 'POST',
                    dataType: "json",
                    async: true,
                    data: {
                        info:info
                    },
                    success: function (res) {
                        if (res.code == 200) {
                            alert('提交成功');
                        }
                    }, error: function (res) {
                        alert(JSON.stringify(res));
                    }
                })
            },
            getCamera: function () {
                console.log("打开照相机");
                this.imgChose = 1;
                $(".imgcamera")[0].click();

            },
            getLibrary: function () {
                console.log("打开相册");
                this.imgChose = 0;
                $(".imgchose")[0].click();

            },
            checkButtonIsValid() {
                var button = $("#add-appointment")
                if (_this.staffName == null || _this.staffName == '') {
                    button.attr('disabled', true);
                    button.css('background-color', '#E4E6E9');
                    button.css('color', '#B5B5B5');
                } else if (_this.staffPhone == null || _this.staffPhone == '') {
                    button.attr('disabled', true);
                    button.css('background-color', '#E4E6E9');
                    button.css('color', '#B5B5B5');
                } else if (_this.faceBase64 == '') {
                    button.attr('disabled', true);
                    button.css('background-color', '#E4E6E9');
                    button.css('color', '#B5B5B5');
                } else {
                    button.attr('disabled', false);
                    button.css('background-color', '#6496EF');
                    button.css('color', '#ffffff');
                }

            },
            handleChange(value) {
                _this.showDate = value.Format("yyyy-MM-dd");
                _this.checkButtonIsValid();
            },
            rotateImg: function (img, direction, canvas) {
                //alert(img);
                //最小与最大旋转方向，图片旋转4次后回到原方向
                var min_step = 0;
                var max_step = 3;
                //var img = document.getElementById(pid);
                if (img == null) return;
                //img的高度和宽度不能在img元素隐藏后获取，否则会出错
                var height = img.height;
                var width = img.width;
                //var step = img.getAttribute('step');
                var step = 2;
                if (step == null) {
                    step = min_step;
                }
                if (direction == 'right') {
                    step++;
                    //旋转到原位置，即超过最大值
                    step > max_step && (step = min_step);
                } else {
                    step--;
                    step < min_step && (step = max_step);
                }
                //img.setAttribute('step', step);
                /*var canvas = document.getElementById('pic_' + pid);
                 if (canvas == null) {
                 img.style.display = 'none';
                 canvas = document.createElement('canvas');
                 canvas.setAttribute('id', 'pic_' + pid);
                 img.parentNode.appendChild(canvas);
                 }  */
                //旋转角度以弧度值为参数
                var degree = step * 90 * Math.PI / 180;
                var ctx = canvas.getContext('2d');
                switch (step) {
                    case 0:
                        canvas.width = width;
                        canvas.height = height;
                        ctx.drawImage(img, 0, 0);
                        break;
                    case 1:
                        canvas.width = height;
                        canvas.height = width;
                        ctx.rotate(degree);
                        ctx.drawImage(img, 0, -height);
                        break;
                    case 2:
                        canvas.width = width;
                        canvas.height = height;
                        ctx.rotate(degree);
                        ctx.drawImage(img, -width, -height);
                        break;
                    case 3:
                        canvas.width = height;
                        canvas.height = width;
                        ctx.rotate(degree);
                        ctx.drawImage(img, -width, 0);
                        break;
                }
            },
            openPicker() {
                console.log("打开时间控件")
                this.$refs.picker.open();
            },

            upload() {
                _this.loading = true;
                var file = null;
                if (_this.imgChose == 1) {
                    file = $(".imgcamera")[0].files['0'];
                } else if (_this.imgChose == 0) {
                    file = $(".imgchose")[0].files['0'];
                } else {
                    return;
                }
                //图片方向角 added by lzk
                var Orientation = null;

                if (file) {
                    console.log("正在上传,请稍后...");
                    var rFilter = /^(image\/jpeg|image\/png)$/i; // 检查图片格式
                    if (!rFilter.test(file.type)) {
                        //showMyTips("请选择jpeg、png格式的图片", false);
                        return;
                    }
                    // var URL = URL || webkitURL;
                    //获取照片方向角属性，用户旋转控制
                    EXIF.getData(file, function () {
                        // alert(EXIF.pretty(this));
                        EXIF.getAllTags(this);
                        //alert(EXIF.getTag(this, 'Orientation'));
                        Orientation = EXIF.getTag(this, 'Orientation');
                        //return;
                    });

                    var oReader = new FileReader();
                    oReader.onload = function (e) {
                        //var blob = URL.createObjectURL(file);
                        //_compress(blob, file, basePath);
                        var image = new Image();
                        image.src = e.target.result;
                        image.onload = function () {
                            var expectWidth = this.naturalWidth;
                            var expectHeight = this.naturalHeight;

                            if (this.naturalWidth > this.naturalHeight && this.naturalWidth > 800) {
                                expectWidth = 800;
                                expectHeight = expectWidth * this.naturalHeight / this.naturalWidth;
                            } else if (this.naturalHeight > this.naturalWidth && this.naturalHeight > 1200) {
                                expectHeight = 1200;
                                expectWidth = expectHeight * this.naturalWidth / this.naturalHeight;
                            }
                            var canvas = document.createElement("canvas");
                            var ctx = canvas.getContext("2d");
                            canvas.width = expectWidth;
                            canvas.height = expectHeight;
                            ctx.drawImage(this, 0, 0, expectWidth, expectHeight);
                            var base64 = null;
                            //修复ios

                            //alert(Orientation);
                            if (Orientation != "" && Orientation != 1) {
                                //alert('旋转处理');
                                switch (Orientation) {
                                    case 6://需要顺时针（向左）90度旋转
                                        //alert('需要顺时针（向左）90度旋转');
                                        _this.rotateImg(this, 'left', canvas);
                                        break;
                                    case 8://需要逆时针（向右）90度旋转
                                        //alert('需要顺时针（向右）90度旋转');
                                        _this.rotateImg(this, 'right', canvas);
                                        break;
                                    case 3://需要180度旋转
                                        //alert('需要180度旋转');
                                        _this.rotateImg(this, 'right', canvas);//转两次
                                        _this.rotateImg(this, 'right', canvas);
                                        break;
                                }
                            }
                            base64 = canvas.toDataURL("image/jpeg", 0.5);
                            let _base64 = base64;
                            _this.loading = false;
                            _this.faceBase64 = _base64;

                            //uploadImage(base64);
                            setTimeout(function () {
                                let tracker = new window.tracking.ObjectTracker('face')
                                tracking.track('#imgBase64', tracker, {camera: true});
                                tracker.on('track', function (event) {
                                    if (event.data.length === 0) {
                                        alert("照片质量不符")
                                        _this.faceBase64 = '';
                                    } else {
                                        event.data.forEach(function (rect) {
//                                        console.log(event)
                                            console.log(rect)
//                                            draw(rect.x, rect.y, rect.width, rect.height);

                                            _this.checkButtonIsValid();
                                        });
                                    }
                                })
                            }, 500);
                        };
                    };
                    oReader.readAsDataURL(file);
                } else {
                    _this.loading = false;
                }
            }
            ,
            setAvatar() {
                this.sheetVisible = true;
            },
            checkName() {
                _this.checkButtonIsValid();
            },
            checkPhone() {
                if (11 == _this.staffPhone.length) {
                    $("#visit-emp-phone").css({color: 'black'})
                    return;
                }
                $("#visit-emp-phone").css({color: 'red'})
                _this.checkButtonIsValid();

            },
            getVisitType(object) {
                var button = $(".switch-type button");
                button.removeClass("index");
                //visitType = $(object).text();
                var $button = $(button[object])
                $button.addClass("index");
                _this.visitPurpose = object;
                //alert($button.text());
                _this.checkButtonIsValid();
            }
            ,
            setGroup(id) {
                var group_type = $(".group-type");
                group_type.removeClass("index");
                $(group_type[id]).addClass("index");
                _this.group = id;
                _this.checkButtonIsValid();
            }
        }, created() {
            _this.tag.tagAll = tagArray.getStaffTag();
            _this.tag.tagShowList = _this.tag.tagAll;
        }
    }
</script>

<style scoped>
    @import "../assets/css/make.appointment.css";


    input {
        border: 0px;
        outline: none;
    }

    .follow-info .el-row {
        padding: 8% 0;
    }

    .group-type {
        border-radius: 0;
        margin: 0;
        border: 0.1rem solid #E5E5E5;
        padding: 4% 5%;
        width: 100%;
        border-left: 0;
        border-right: 0;
        outline: none;
        color: #A9A9A9;

    }

    .group-type.index {
        border-radius: 0;
        margin: 0;
        border: 0.1rem solid #14509F;
        background-color: #14509F;
        border-left: 0;
        border-right: 0;
        outline: none;
        color: #fff;

    }

    .switch-type .el-col > button.index {
        background-color: #14509F;
        outline: none;
        color: #FFFFFF;
        border: 0.1rem solid #14509F;
    }

    .switch-type .el-col > button {
        border-radius: 0;
        background-color: #fff;
        outline: none;
        padding: 10% 10%;
        width: 90%;
        color: #8D8D8D;
        border: 0.1rem solid #E5E5E5;
    }

    .invitation-info {
        width: 85%;
        margin: 0 auto;
    }

    .el-col {
        color: #999999;
        font-size: 1rem;
        text-align: left;
    }

    .input-info {
        border-bottom: 1px solid #e1e1e1;
        width: 100%;
    }

    .el-row {
        margin: 0 auto;
        padding: 6% 0;
    }

    .img {
        display: -webkit-box;
        -webkit-box-orient: horizontal;
        -webkit-box-pack: center;
        -webkit-box-align: center;

        display: -moz-box;
        -moz-box-orient: horizontal;
        -moz-box-pack: center;
        -moz-box-align: center;

        text-align: center;
        padding: 0;
        height: 10rem;
        background: #F0F0F0;
        border: 1px solid #F0F0F0;
    }

    #imgBase64 {
        height: 100%;
        object-fit: contain;
    }

    .el-input__inner {
        -webkit-appearance: none;
        background-color: #FFF;
        background-image: none;
        border-radius: 4px;
        border: 0px solid #DCDFE6;
        -webkit-box-sizing: border-box;
        box-sizing: border-box;
        color: #606266;
        display: inline-block;
        font-size: inherit;
        height: 40px;
        line-height: 40px;
        outline: 0;
        padding: 0 15px;
        -webkit-transition: border-color .2s cubic-bezier(.645,.045,.355,1);
        transition: border-color .2s cubic-bezier(.645,.045,.355,1);
        width: 100%;
    }
</style>