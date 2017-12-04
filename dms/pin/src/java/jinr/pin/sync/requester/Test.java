package jinr.pin.sync.requester;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.event.WindowStateListener;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.JButton;
import javax.swing.JFrame;

import org.Base64;

import jinr.pin.sync.ConnectionHelper;
import jinr.pin.sync.KeysHelper;
import jinr.pin.sync.model.CommonRequest;
import jinr.pin.sync.model.CommonResponse;

public class Test {
	public static void main(String[] args) throws Exception {
//		testRequestHelper();
		
//		String a = "ICAgINCS0YHQtdC8INC/0YDQuNCy0LXRgi4NCg0KICAg0JLRi9C00LDQu9C+0YHRjCDQvdC10LzQvdC+0LPQviDRgdCy0L7QsdC+0LTQvdC+0LPQviDQstGA0LXQvNC10L3QuCwg0YHQvtC70L3RhtC1INC30LAg0L7QutC90L7QvCDQttCw0YDQuNGCLCDRgtCw0Log0YfRgtC+INGPIA0K0YHQv9Cw0YHRgdGPINCyINGC0LXQvdGR0Log0Lgg0L/QvtC/0YvRgtCw0Y7RgdGMINC90LDQsdGA0LDRgtGMINC+0YfQtdGA0LXQtNC90L7QuSDRgdGA0LXQtyDQvtGJ0YPRidC10L3QuNC5INC+0YIg0YHRgtGA0LDQvdGLLg0KDQogICDQodC10LPQvtC00L3RjyDRgSDRg9GC0YDQsCDRgdGK0LXQt9C00LjQuyDQsiDQuNGFINC80LXRgdGC0L3Rg9GOINC/0YDQvtGC0LXRgdGC0LDQvdGC0YHQutGD0Y4g0YbQtdGA0LrQvtCy0YwuINCd0LDRhdC+0LTQuNGC0YHRjyANCtC+0L3QsCDQvdC10LTQsNC70LXQutC+INC+0YIg0YbQtdC90YLRgNCwINCz0L7RgNC+0LTQsCwg0YDRj9C00L7QvCDRgSDQvdC10Lkg0LrRgNGD0L/QvdCw0Y8g0YHRgtC+0Y/QvdC60LAuINCi0LjQv9C+0LLQsNGPIA0K0L/RgNC+0YLQtdGB0YLQsNC90YHQutCw0Y8g0YbQtdGA0LrQvtCy0YwgLSDRjdGC0L4g0L3QtSDQvNC10YHRgtC+LCDRjdGC0L4g0L7RgNCz0LDQvdC40LfQsNGG0LjRjy4g0KHQstC+0LggItGB0LvRg9C20LHRiyIg0L7QvdCwIA0K0L/RgNC+0LLQvtC00LjRgiDQvtCx0YvRh9C90L4g0LIg0L/QvtC80LXRidC10L3QuNGP0YUg0YEg0YXQvtGA0L7RiNC10Lkg0LDQutGD0YHRgtC40LrQvtC5LCDRg9C00L7QsdC90L4g0YDQsNGB0L/QvtC70L7QttC10L3QvdGL0YUg0LggDQrQv9GA0L7RgdGC0L7RgNC90YvRhSwg0YLQsNC60LjQtSDQvNC10YHRgtCwINCx0L7Qu9GM0YjQtSDQstGB0LXQs9C+INC/0L7QtNGF0L7QtNGP0YIg0LTQu9GPINGA0L7Qui3QutC+0L3RhtC10YDRgtC+0LIg0LjQu9C4IA0K0LDQs9C40YLQsNGG0LjQvtC90L3Ri9GFINC80LjRgtC40L3Qs9C+0LIg0LIgINC30LDQutGA0YvRgtC+0Lwg0L/QvtC80LXRidC10L3QuNC4ICjRh9C10Lwg0YHQvtCx0YHRgtCy0LXQvdC90L4g0Lgg0Y/QstC70Y/QtdGC0YHRjyANCtC00LXRj9GC0LXQu9GM0L3QvtGB0YLRjCDQu9GO0LHQvtC5INGG0LXRgNC60LLQuCkuINCV0YHQu9C4INC60YLQviDQsdGL0Lsg0LIg0L7QtNC90L7QuSDQuNC3INC80L7RgdC60L7QstGB0LrQuNGFIA0K0L/RgNC+0YLQtdGB0YLQsNC90YLRgdC60LjRhSDRhtC10YDQutCy0LXQuSDQvdCwICLRgdC70YPQttCx0LUiIC0g0L/RgNC40LzQtdGA0L3QviDQt9C90LDQtdGC0LUgKNC/0YDQsNCy0LTQsCDQvNCw0YHRiNGC0LDQsSDRgtGD0YIgDQrQutC+0L3QtdGH0L3QviDQvdCw0LzQvdC+0LPQviDRgdC10YDRjNC10LfQvdC10LkpLiDQmtGC0L4g0L3QtSDQsdGL0LsgLSDQv9C+0YHQvNC+0YLRgNC40YLQtSDQu9GO0LHQvtC5INCz0L7Qu9C40LLRg9C00YHQutC40LkgDQrRhNC40LvRjNC8LdC80Y7Qt9C40LrQuyDQv9GA0L4g0LDQvNC10YDQuNC60LDQvdGB0LrRg9GOINGG0LXRgNC60L7QstGMIC0gINCy0L7RgiDRjdGC0L4g0L7QvdC+INC4INC10YHRgtGMLiDQoSDRgtC+0LkgDQrRgNCw0LfQvdC40YbQtdC5LCDRh9GC0L4g0LfQtNC10YHRjCDQstGB0LUg0LHRi9C70L4g0LLQttC40LLRg9GOLg0KICAg0JIg0L/RgNC40L3RhtC40L/QtSDRgNC10LHRj9GC0LAt0L/RgNC+0YLQtdGB0YLQsNC90YLRiyDQuNGB0L/QvtCy0LXQtNGD0Y7RgiDQvtGH0LXQvdGMINC/0YDQvtGB0YLRg9GOINC60L7QvdGG0LXQv9GG0LjRjjogDQrQu9GO0LTQuCDQu9GO0LHRj9GCINC/0LXRgtGMLCDQu9GO0LTQuCDQvdC1INC70Y7QsdGP0YIg0L7QtNC40L3QvtGH0LXRgdGC0LLQsCwg0LvRjtC00Y/QvCDQvdGA0LDQstC40YLRjNGB0Y8g0L7QsdGJ0LDRgtGM0YHRjyAo0LggDQrQv9GA0Lgg0Y3RgtC+0Lwg0L3QtSDRgdC70LjRiNC60L7QvCDQsdC70LjQt9C60L4g0LTRgNGD0LMg0LTRgNGD0LPQsCDRg9C30L3QsNCy0LDRgtGMKSDQuCDRgyDQvdC40YUg0LXRgdGC0Ywg0YHQstC+0LHQvtC00L3QvtC1IA0K0LLRgNC10LzRjy4g0KLQtdC/0LXRgNGMINC6INGN0YLQvtC80YMg0L3QsNC00L4g0LTQvtCx0LDQstC40YLRjCDQvdC10LzQvdC+0LbQutC+INC20LjQt9C90LXQvdC90L7QuSDRjdC90LXRgNCz0LjQuCwg0YXQsNGA0LjQt9C80YsgDQrQv9GA0L7Qv9C+0LLQtdC00L3QuNC60LAgKCDQviDRh9C10Lwg0L/RgNC+0L/QvtCy0LXQtNC+0LLQsNGC0YwgLSDQvdC1INGB0YPRgtGMLCDQs9C70LDQstC90L7QtSDRh9GC0L7QsdGLIA0K0YXQsNGA0LjQt9C80LDRgtC40YfQvdC+ISEhKSwg0LTQvtCx0LDQstC40YLRjCDRg9C00L7QsdGB0YLQstCwICjRgtC40L/QsCDQv9Cw0YDQutC+0LLQutC4INC4INC60L7QvNC90LDRgtGLINC00LvRjyDRgdC00LDRh9C4IA0K0LTQtdGC0LXQuSDQsiAi0L7QsdC10LfRitGP0L3QvdC40LoiINC00LvRjyDRgdC+0LLQvNC10YHRgtC90YvRhSDQuNCz0YAgLSDQuCDQtNC10YLRj9GFINGF0L7RgNC+0YjQviDQuCDRgyDRgNC+0LTQuNGC0LXQu9C10LkgDQrQs9C+0LvQvtCy0LAg0L3QtSDQsdC+0LvQuNGCKSwg0LTQvtCx0LDQstC40YLRjCDQutCw0LrRg9GOLdC90LjQsdGD0LTRjCDQvNCw0YHRgdC+0LLRg9GOICLQsdC+0LPQvtGD0LPQvtC00L3Rg9GOIA0K0LTQtdGP0YLQtdC70YzQvdC+0YHRgtGMIiAo0YHQvtCx0YDQsNGC0Ywg0LTQtdC90LXQsyDQvdCwIDUwMDAg0L3QtdCx0L7Qu9GM0YjQuNGFINGA0L7QttC00LXRgdGC0LLQtdC90YHQutC40YUg0L/QvtC00LDRgNC60L7QsiANCtC00LvRjyDQvNCw0LvQvtC40LzRg9GJ0LjRhSwg0LfQsNC/0LDQutC+0LLQsNGC0Ywg0LjRhSDQuCDRgNCw0LfQvtGB0LvQsNGC0YwpIC0g0LLRg9Cw0LvRjy4g0JLQvtGCINCy0LDQvCDQtNC10LnRgdGC0LLRg9GO0YnQsNGPIA0K0LzQvtC00LXQu9GMINC/0YDQvtGC0LXRgdGC0LDQvdGB0LrQvtC5INGG0LXRgNC60LLQuC4NCiAgICDQk9C70LDQstC90L7QtSwg0YfRgtC+0LHRiyDQvdCw0YDQvtC0INCy0LXRgNC40LssINGH0YLQviDRjdGC0L4g0L/RgNCw0LLQuNC70YzQvdC+LiDQkCDRhdC+0YDQvtGI0LDRjyDQvNGD0LfRi9C60LAgDQoo0YHQv9C+0YHQvtCx0L3QvtGB0YLQuCDQuiDQvNGD0LfRi9C60LUg0L7RgiDRgNC10LvQuNCz0LjQvtC30L3Ri9GFINGD0LHQtdC20LTQtdC90LjQuSDQvdC1INC30LDQstC40YHRj9GCKSwg0YXQvtGA0L7RiNC40LUgDQrRgtC10LrRgdGC0Ysg0L3QsCDQv9GA0L7RgdGC0L7QvCDQsNC90LPQu9C40LnRgdC60L7QvCAo0LTQsNC20LUg0Y8g0YEg0L/QtdGA0LLQvtCz0L4g0YDQsNC30LAg0YHQvNC+0LMg0LLRgdC1INC/0YDQvtGH0LXRgdGC0Ywg0LggDQrQv9C+0L3Rj9GC0YwpLCDRhdC+0YDQvtGI0LjQtSDQs9C+0LvQvtGB0LAg0LjRgdC/0L7Qu9C90LjRgtC10LvQtdC5ICjQvtC/0Y/RgtGMINC20LUsINGB0L/QvtGB0L7QsdC90L7RgdGC0Lgg0Log0LzRg9C30YvQutC1INC+0YIgDQrRgNC10LvQuNCz0LjQvtC30L3Ri9GFINGD0LHQtdC20LTQtdC90LjQuSDQvdC1INC30LDQstC40YHRj9GCKSDQuCDRhdCw0YDQuNC30LzQsNGC0LjRh9C90LDRjyDQv9GA0L7Qv9C+0LLQtdC00Ywg0L/QvtC80L7Qs9GD0YIgDQrQu9GO0LTRj9C8INC/0L7Rh9GD0LLRgdGC0LLQvtCy0LDRgtGMINGB0LXQsdGPINCx0LvQuNC20LUg0Lgg0YDQvtC00L3QtdC1INC00YDRg9CzINC6INC00YDRg9Cz0YMgKNCyINGC0LXRh9C10L3QuNC1ICLRgdC70YPQttCx0YsiIA0K0L7QsdGP0LfQsNGC0LXQu9GM0L3QsNGPINGH0LDRgdGC0YwgLSDQv9GA0LjQstC10YLRgdGC0LLQuNC1INC90L7QstC10L3RjNC60LjRhSwg0YDRg9C60L7Qv9C+0LbQsNGC0LjQtSDRgSDRgdC+0YHQtdC00L7QvCDQuCANCtC60L7RgNC+0YLQutC40Lkg0YDQsNC30LPQvtCy0L7RgCDQvdCwINCw0LHRgdGC0YDQsNC60YLQvdGD0Y4g0YLQtdC80YMsINCyINC+0LHRidC10Lwg0YHRgtCw0L3QtNCw0YDRgtC90YvQuSDQvdCw0LHQvtGAINC70Y7QsdC+0LPQviANCtC/0YHQuNGF0L7QvdCw0YDQutC+0YLQuNGH0LXRgdC60L7Qs9C+INC80LXRgNC+0L/RgNC40Y/RgtC40Y8gOikgKS4g0J3QtSDRgdGC0L7QuNGCINC30LDQsdGL0LLQsNGC0YwsINGH0YLQviDQt9C00LXRgdGMINC/0YDQuCANCtCy0YHQtdC+0LHRidC10Lkg0LLQtdC20LvQuNCy0L7RgdGC0Lgg0Lgg0LPQvtGB0YLQtdC/0YDQuNC40LzQvdC+0YHRgtC4ICjRjdGC0L4g0L/RgNCw0LLQtNCwINGC0LDQuiDQuCDQtdGB0YLRjCkg0L3QsNGA0L7QtCANCtC20LjQstC10YIg0L/RgNC4INCy0L/QvtC70L3QtSDRgdC10LHQtSDRgNCw0LfQstC40YLQvtC8INC60LDQv9C40YLQsNC70LjQt9C80LUuINCi0L4g0LXRgdGC0Ywg0Y3RgtC+INC90L7RgNC80LDQu9GM0L3QviANCtC+0YDQs9Cw0L3QuNC30L7QstGL0LLQsNGC0Ywg0LIg0LPQvtGA0L7QtNCw0YUg0LzQvtC90L7Qv9C+0LvQuNC4ICjRh9C40YLQsNC5INCU0YDQsNC50LfQtdGA0L7QstGB0LrQvtCz0L4gItCk0LjQvdCw0L3RgdC40YHRgtCwIiksIA0K0LLQstC+0LTRjyDRg9C80L7Qv9C+0LzRgNCw0YfQuNGC0LXQu9GM0L3Rg9GOINGB0YLQvtC40LzQvtGB0YLRjCDQv9GA0L7QtdC30LTQsCDQvdCwINGC0YDQsNC90YHQv9C+0YDRgtC1LiDQrdGC0L4g0L3QvtGA0LzQsNC70YzQvdC+IA0K0LLQtdGB0YzQvNCwINC80LDQu9C+INC40L3RgtC10YDQtdGB0L7QstCw0YLRjNGB0Y8g0YHQvtGB0LXQtNGP0LzQuCDQstC+0LrRgNGD0LMuINCt0YLQviDQvdC+0YDQvNCw0LvRjNC90L4g0LrQvtCz0LTQsCDQsiDRgNCw0LnQvtC90LUgDQrQtdC00LjQvdGB0YLQstC10L3QvdC+0LUgItC90LjRh9C10LnQvdC+0LUiINC80LXRgdGC0L4sINCz0LTQtSDQvNC+0LPRg9GCINC/0L7QuNCz0YDQsNGC0Ywg0LTQtdGC0LggLSDQv9C70Y/Qti4g0J/QvtGC0L7QvNGDIA0K0YfRgtC+INC00LDQttC1INGI0LrQvtC70YzQvdGL0Lkg0LTQstC+0YAg0L3QtSDQvdC40YfQtdC50L3Ri9C5LiDQmCDQtNCw0LbQtSDQtdGB0LvQuCDRjdGC0L4g0YLQstC+0Y8g0YjQutC+0LvQsCAtINCy0YDQtdC80Y8gDQrQv9GA0LXQsdGL0LLQsNC90LjRjyDQvtCz0YDQsNC90LjRh9C10L3QviDQstGA0LXQvNC10L3QtdC8INC+0LHRg9GH0LXQvdC40Y8uINCi0YPRgiDQvdC10YIg0YLQsNC60L7Qs9C+INC/0L7QvdGP0YLQuNGPLCDQutCw0LogDQoi0LTQstC+0YAg0LzQvtC10LPQviDQtNC+0LzQsCIuINCf0L7RgtC+0LzRgyDRh9GC0L4gOTUlINC20LjRgtC10LvQtdC5INCe0LrQu9C10L3QtNCwINC20LjQstGD0YIg0LIg0YfQsNGB0YLQvdGL0YUg0LTQvtC80LDRhSwgDQrQstC10YHRjNC80LAg0LHQtdC30L7QsdGA0LDQt9C90L4g0YDQsNC30LzQtdGJ0LXQvdC90YvRhSDQvdCwINGD0YfQsNGB0YLQutCw0YUg0LIgOC0xMCDRgdC+0YLQvtC6LiDQn9GA0LjQvNC40YLQtSDQstC+IA0K0LLQvdC40LzQsNC90LjQtSwg0YfRgtC+INGN0YLQviDQvdC1INC00LXRgNC10LLQtdC90YHQutCw0Y8g0LfQsNGB0YLRgNC+0LnQutCwIC0g0Y3RgtC+INGH0LDRgdGC0L3QvtGB0L7QsdGB0YLQstC10L3QvdC40YfQtdGB0LrQsNGPIA0K0LfQsNGB0YLRgNC+0LnQutCwICjRgtC+INC10YHRgtGMINC00L7RgNC+0LPQuCDQvNC10LbQtNGDINGD0YfQsNGB0YLQutCw0LzQuCDQv9GA0LXQtNGD0YHQvNC+0YLRgNC10L3QvdGLINGC0L7Qu9GM0LrQviDRgtCw0LwsINCz0LTQtSANCtC/0YDQvtGF0L7QtNGP0YIg0LTQvtGA0L7Qs9C4INCw0LLRgtC+0LzQvtCx0LjQu9GM0L3Ri9C1LiDQldGB0LvQuCDQsNCy0YLQvtC00L7RgNC+0LPQuCDQvdC1INGB0L7QtdC00LjQvdC10L3RiyDQsiDQutCw0LrQvtC8INGC0L4gDQrQvNC10YHRgtC1LCDRgtC+INGN0YLQviDRgSDQstC10YDQvtGP0YLQvdC+0YHRgtGM0Y4gOTksOSUg0L7Qt9C90LDRh9Cw0LXRgiwg0YfRgtC+INC/0LXRiNC60L7QvCDQv9GA0L7QudGC0Lgg0LLQsNC8INGC0LDQvCANCtGC0L7QttC1INC90LUg0YPQtNCw0YHRgtGB0Y8pLiDQstC10YHRjNC80LAg0YHQstC+0LXQvtCx0YDQsNC30L3QsNGPINGB0LjRgdGC0LXQvNCwINC80YvRiNC70LXQvdC40Y8sINC90LUg0L/RgNCw0LLQtNCwINC70LguIA0K0J/QvtGN0YLQvtC80YMg0YDQtdC70LjQs9C40Y8gKNGC0L7Rh9C90LXQtSDQsdGL0LvQviDQsdGLINGB0LrQsNC30LDRgtGMLCDQv9C+0YHQtdGJ0LXQvdC40LUgItGB0LvRg9C20LEiKSAtINCy0LXRgdGM0LzQsCANCtGA0LDRgdC/0YDQvtGB0YLRgNCw0L3QtdC90L3Ri9C5INGB0L/QvtGB0L7QsSDQv9C+0YfRg9Cy0YHRgtCy0L7QstCw0YLRjCDRgdC10LHRjyDQsdC70LjQttC1INC6INC90LDRgNC+0LTRgy4g0K8g0YLRg9GCINC+0LHRgNCw0YLQuNC7IA0K0LLQvdC40LzQsNC90LjQtSwg0YfRgtC+INGDINC00LXRgtC10Lkg0LzQtdGB0YLQvdC+0Lkg0YHQtdC80YzQuCDQvdC1INC+0YHQvtCx0L4t0YLQviDQvNC90L7Qs9C+INC00YDRg9C30LXQuSwg0LAg0LXRgdC70Lgg0L7QvdC4IA0K0Lgg0LXRgdGC0YwsINGC0L4g0L7QsdC40YLQsNGO0YLRgdGPINC70LjQsdC+INGB0LvQuNGI0LrQvtC8INC00LDQu9C10LrQviAo0L3QuNC60YLQviDQvdC1INGA0LDQt9GDINC90LUg0LfQsNGI0LXQuyDQv9GA0L7RgdGC0L4gDQrRgtCw0Log0LIg0LPQvtGB0YLQuCwg0L/QsNGA0YMg0YDQsNC3INC/0YDQsNCy0LTQsCDQv9GA0LjQtdC30LbQsNC70LAg0L3QsCDQvNCw0YjQuNC90LDRhSDQstC80LXRgdGC0LUg0YEg0YDQvtC00LjRgtC10LvRj9C80LgpLiANCtCd0LjQutGC0L4g0L3QuCDRgNCw0LfRgyDQvdC1INC/0L7Qt9Cy0L7QvdC40Lsg0YEg0LLQvtC/0YDQvtGB0L7QvCwg0LAg0LzQvtC20L3QviDQu9C4INC6INGC0LXQsdC1INC/0YDQuNC50YLQuCDQv9C+0LjQs9GA0LDRgtGMIA0K0LIg0L/RgNC40YHRgtCw0LLQutGDICjRgtGD0YIg0L7QvdC4INC10YHRgtGMINCyINC60LDQttC00L7QvCDQtNC+0LzQtSwg0L3QviDQstC80LXRgdGC0LUg0LLQtdC00Ywg0L3QsNC80L3QvtCz0L4g0LLQtdGB0LXQu9C10LkpLg0KICAgINCf0L7QutCwINGH0YLQviDRjyDRgdC60LvQvtC90LXQvSDQv9C+0LvQsNCz0LDRgtGMLCDRh9GC0L4g0Y3RgtC+INGB0LLRj9C30LDQvdC+INGC0L7Qu9GM0LrQviDQu9C40YjRjCDRgSDQtNCw0L3QvdC+0LkgDQrQutC+0L3QutGA0LXRgtC90L7QuSDRgdC10LzRjNC10LkuINCSINGN0YLQvtC8INGA0LDQudC+0L3QtSDQv9C+0LvQvdC+INC00L7QvNC+0LIg0YEg0LTQtdGC0YzQvNC4LCDRjyDQstC40LTQtdC7LCDRh9GC0L4g0YLQsNC8IA0K0LLRgNC+0LTQtSDQutCw0Log0LHQvtC70LXQtSDQsNC60YLQuNCy0L3QsNGPINC20LjQt9C90YwuINCSINC70Y7QsdC+0Lwg0YHQu9GD0YfQsNC1INCy0LfRj9C7INGB0LXQsdC1INGN0YLQviDQvdCwINC30LDQvNC10YLQutGDLg0KDQogICDQldGJ0LUg0L7QtNC90LAg0LfQsNC80LXRgtC60LAsINC90LUg0LIg0L/RgNC+0LTQvtC70LbQtdC90LjQtSDRgtC10LzRiywg0LAg0L/RgNC+0YHRgtC+INGC0LDQui4NCiAgINCd0LAg0L7QsdGA0LDRgtC90L7QvCDQv9GD0YLQuCDQtNC+0LHQuNGA0LDQu9GB0Y8g0L3QsCDQsNCy0YLQvtCx0YPRgdC1INC40Lcg0L/RgNC40LPQvtGA0L7QtNCwINC10YnQtSDQsdC+0LvQtdC1IA0K0YPQtNCw0LvQtdC90L3QvtCz0L4sINGH0LXQvCDQvNC+0Lkg0YXQvtGD0LzRgdGC0LXQuSAo0LXRgdC70Lgg0LrRgtC+INGD0LbQtSDQv9C+0YHQvNC+0YLRgNC10Lsg0L/RgNC+INCw0LLRgtC+0LHRg9GB0YssINGC0L4gDQrRjdGC0L4gNCDRgdGC0LXQudC00LbQsCDQvtGCINGG0LXQvdGC0YDQsCDQntC60LvQtdC90LTQsCkuINGD0LvQvtGH0LrQuCDQsiDQv9GA0LjQvdGG0LjQv9C1INC80LDQu9C10L3RjNC60LjQtSAoMSw1IA0K0L/QvtC70L7RgdGLKSwg0L3QviDQvtGH0LXQvdGMINCw0LrQutGD0YDQsNGC0L3Ri9C1INC4INGD0YXQvtC20LXQvdC90YvQtS4g0JLQtdC30LTQtSDQt9C90LDQutC4LCDQstC10LfQtNC1INGC0YDQvtGC0YPQsNGAIA0K0LXRgdGC0YwuINCd0L4g0LfQsNGB0YLRgNC+0LnQutCwINGC0LXRgNGA0LjRgtC+0YDQuNC4INC60LDQutCw0Y8t0YLQviDQsdC10LfRg9C80L3QviDQtNGD0YDQsNGG0LrQsNGPLiDQotCw0LrQvtC1INC+0YnRg9GJ0LXQvdC40LUsIA0K0YfRgtC+INC60LDQutC+0Lkt0YLQviDRgNC40LXQu9GC0L7RgCDRgNC10YjQuNC7INGB0YDRg9Cx0LjRgtGMINCx0LDQsdC70LAg0Lgg0L3QsNGC0YvQutCw0Lsg0L3QsCDQutCw0LbQtNC+0Lwg0L3QsNGA0LXQt9Cw0L3QvdC+0LwgDQrRg9GH0LDRgdGC0LrQtSDQv9C+INC80LDQutGB0LjQvNGD0LzRgyDQstGB0Y/QutC40YUg0L/QvtGB0YLRgNC+0LXQutGCLiDQotC+0LvRjNC60L4g0L/QsNGA0YMg0YDQsNC3INCy0YHRgtGA0LXRgtC40Lsg0LrQsNC60LjQtS3RgtC+IA0K0L/Qu9C+0LTQvtCy0YvQuSDQtNC10YDQtdCy0YzRjyAo0L7QtNC40L0g0LjQtyDRg9GH0LDRgdGC0LrQvtCyINC90LDRhdC+0LTQuNGC0YHRjyDQv9C+0YHQvtGB0LXQtNGB0YLQstGDINGBINC80L7QuNC8IA0K0YXQvtGD0YHRgtC10LXQvCwg0L3QsCDQvdC10Lwg0YDQsNGB0YLQtdGCINC70LjQvNC+0L3QvdC+0LUg0LTQtdGA0LXQstC+INGBINC70LjQvNC+0L3QsNC80LggKNCwINC80L7QttC10YIg0Y3RgtC+INC70LDQudC8IC0gDQrQvdC1INC90LDRgdGC0L7Qu9GM0LrQviDRgdC40LvQtdC9INCyINCx0L7RgtCw0L3QuNC60LUpKS4g0KLQviDQtdGB0YLRjCDQv9GA0LjQs9C+0YDQvtC0INGB0YLQsNGA0LDQu9C40YHRjCDQvNCw0LrRgdC40LzQsNC70YzQvdC+IA0K0L7Qv9GC0LjQvNC40LfQuNGA0L7QstCw0YLRjCDQv9C+0LQg0YDQsNGB0YLRgNCw0YLRgyDQtNC10L3QtdCzLCDQsCDQvdC1INC/0L7QtCDQv9C+0YHRgtC+0Y/QvdC90L7QtSDQv9GA0L7QttC40LLQsNC90LjQtS4gDQrQl9Cw0LHQsNCy0L3Qviwg0YfRgtC+ICLQv9C40L/QuyDRgdGF0LDQstCw0LsiINGC0LDQutC+0Lkg0YDQsNGB0LrQu9Cw0LQsINC00LDQttC1INC90LXRgdC80L7RgtGA0Y8g0L3QsCDRgtC+LCDRh9GC0L4g0L7QvdC4IA0K0YHQsNC80Lgg0LfQsNC/0LvQsNGC0LjQu9C4INC00LXQvdGM0LPQuC4NCg0KINCS0LDRiCDQoNC+0LzQutCwLg0K";
//		
//		System.out.println(new String(Base64.decode(a), "utf8"));
		
//		System.out.println(new CommonRequest<String>().id);
		
//		testKeys();
		
		testRequestWorker();
		
	}

	private static void testRequestHelper() throws Exception, SQLException {
		Class.forName("com.mysql.jdbc.Driver");
		final KeysHelper kh = new KeysHelper(new ConnectionHelper("jdbc:mysql://localhost/pindb", "root", "qqq"));
		
		CommonRequest<String> request = new CommonRequest<String>();
		request.title = "Test Request";
		request.url = "http://pin";
		request.commands.put("SHOW", "SHOW TABLES");
		
		CommonResponse response = new RequestHelper(kh).makeRequest(request);
		
		ResultSet rs = (ResultSet)response.results.get("SHOW");

		while(rs.next()) {
			System.out.println(rs.getRow() + " :: " + rs.getString(1));
		}
	}

	public static class ShowTablesRequest extends CommonRequest<String> {
		private static final long serialVersionUID = -5808465256736218026L;
		final static String SHOW_CMD = "SHOW";
		
		public ShowTablesRequest() {
			title = "SHOW TABLES Request";
			commands.put(SHOW_CMD, "SHOW TABLES");
		}
		
		@Override
		public void onResponse(CommonResponse response) throws Exception {
			ResultSet rs = (ResultSet)response.results.get(SHOW_CMD);
			System.out.println(rs.getFetchSize());
			while(rs.next()) {
				System.out.println(rs.getString(1));
			}
		}
		
		@Override
		public void onResponseError(Throwable ex) throws Exception {
			ex.printStackTrace();
		}
	}
	
	private static void testRequestWorker() throws Exception {
		Class.forName("com.mysql.jdbc.Driver");
		final RequestWorker rw = new RequestWorker(new ConnectionHelper("jdbc:mysql://localhost/pindb", "root", "qqq"));
		
		JFrame win = new JFrame("Queue test window");
		win.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		win.setLocation(300, 300);
		win.setSize(300, 300);
		
		
		win.addWindowListener(new WindowListener(){
			public void windowActivated(WindowEvent e) {}

			public void windowClosed(WindowEvent e) {
				rw.close();
			}

			public void windowClosing(WindowEvent e) {}

			public void windowDeactivated(WindowEvent e) {}

			public void windowDeiconified(WindowEvent e) {}

			public void windowIconified(WindowEvent e) {}

			public void windowOpened(WindowEvent e) {}
		});
		
		JButton requestBtn = new JButton("add request");
		requestBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
//			  rw.addRequest("http://pin", new ShowTablesRequest());  //SK 01.04.09 - compile error
				rw.addRequest("http://pin", null);
			}
		});
		
		win.getContentPane().setLayout(new FlowLayout());
		win.getContentPane().add(requestBtn);
		
		win.setVisible(true);
	}

	private static void testKeys() throws ClassNotFoundException, SQLException, Exception {
		Class.forName("com.mysql.jdbc.Driver");
		KeysHelper kh = new KeysHelper(new ConnectionHelper("jdbc:mysql://localhost/pindb", "root", "qqq"));
		kh.initAndSave("http://pin/");
//		kh.getPrivateKey("localhost");
	}
}
