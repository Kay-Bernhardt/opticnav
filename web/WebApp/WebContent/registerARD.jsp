<t:authpage title="Register Device" js="registerARD.js">
    Currently Registered Device
    <div class="registeredARD"><span id="ard_id"></span><span id="ard_name"></span></div>
    <form id="RegisterARD" action="op/RegisterARD">
    <input type="text" name="code" id="code" placeholder="Confirmation code" autocomplete="off" />
    <input type="submit" value="Register" id="register" />
    <t:message name="message" />
    </form>
</t:authpage>