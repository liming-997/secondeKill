<%--
  Created by IntelliJ IDEA.
  User: Adminster
  Date: 2020/5/27
  Time: 15:02
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>测试秒杀</title>
    <script type="text/javascript" src="jquery/jquery-2.1.1.min.js"></script>
</head>
<body>
    <form>
        <input type="hidden" name="id" value="10001">
        <a href="#">一元秒杀</a>
    </form>
</body>
<script type="text/javascript">
    $("a").click(function () {
        $.ajax({
            type:"post",
            data:$("form").serialize(),
            url:"sk/doSecondKill",
            success:function (data) {
                if (data == "ok") {
                    alert("秒杀成功");
                }else{
                    alert(data);
                    //a标签不能点击
                    $("a").prop("disable", true);
                }
            }
        });
        return false;
    });
</script>
</html>
